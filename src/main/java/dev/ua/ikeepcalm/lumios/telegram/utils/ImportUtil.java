package dev.ua.ikeepcalm.lumios.telegram.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.campus.CampusDay;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.campus.CampusTimetable;
import dev.ua.ikeepcalm.lumios.telegram.exceptions.CampusApiException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;
import java.util.concurrent.TimeUnit;

/**
 * Thin client for the public KPI Campus schedule API.
 * <p>
 * Two endpoints are used:
 * <ul>
 *     <li>{@code /schedule/groups} - redirects to a CDN dump of every group ({@code id}, {@code name},
 *     {@code faculty}). Its {@code filter} parameter is ignored by the server, so filtering happens here.</li>
 *     <li>{@code /schedule/lessons?groupId=} - the two-week schedule for one group. Only the numeric
 *     group id from the dump is accepted; a group name is silently ignored and yields an empty schedule.</li>
 * </ul>
 * The group dump is ~120 KB and changes at most a couple of times per semester, so it is cached in
 * memory; a stale copy is preferred over failing when the API is temporarily unavailable.
 */
public class ImportUtil {

    private static final Logger log = LoggerFactory.getLogger(ImportUtil.class);

    private static final String GROUPS_ENDPOINT = "https://api.campus.kpi.ua/schedule/groups";
    private static final String LESSONS_ENDPOINT = "https://api.campus.kpi.ua/schedule/lessons";

    private static final int CONNECT_TIMEOUT_MS = 5_000;
    private static final int READ_TIMEOUT_MS = 15_000;
    private static final int MAX_REDIRECTS = 3;
    private static final long CACHE_TTL_MS = TimeUnit.HOURS.toMillis(6);

    public static final int MIN_QUERY_LENGTH = 2;
    /**
     * Keeps {@code import#p#<page>#<query>} callback payloads inside Telegram's 64-byte limit
     * even when every character of the query is two-byte Cyrillic.
     */
    public static final int MAX_QUERY_LENGTH = 24;

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private static final Object CACHE_LOCK = new Object();
    private static volatile List<CampusGroup> cachedGroups;
    private static volatile long cachedAt;

    /**
     * Ukrainian to Latin transliteration, so a group typed in Latin ("IP-61", "KV-63") still finds
     * its Cyrillic counterpart. Group names themselves are pure Cyrillic.
     */
    private static final Map<Character, String> CYRILLIC_TO_LATIN = Map.ofEntries(
            Map.entry('а', "a"), Map.entry('б', "b"), Map.entry('в', "v"), Map.entry('г', "h"),
            Map.entry('ґ', "g"), Map.entry('д', "d"), Map.entry('е', "e"), Map.entry('є', "ie"),
            Map.entry('ж', "zh"), Map.entry('з', "z"), Map.entry('и', "i"), Map.entry('і', "i"),
            Map.entry('ї', "i"), Map.entry('й', "i"), Map.entry('к', "k"), Map.entry('л', "l"),
            Map.entry('м', "m"), Map.entry('н', "n"), Map.entry('о', "o"), Map.entry('п', "p"),
            Map.entry('р', "r"), Map.entry('с', "s"), Map.entry('т', "t"), Map.entry('у', "u"),
            Map.entry('ф', "f"), Map.entry('х', "kh"), Map.entry('ц', "ts"), Map.entry('ч', "ch"),
            Map.entry('ш', "sh"), Map.entry('щ', "shch"), Map.entry('ь', ""), Map.entry('ю', "iu"),
            Map.entry('я', "ia"));

    /**
     * Latin letters with no place in the transliteration above, folded onto the sound they stand for.
     */
    private static final Map<Character, String> LATIN_ALIASES =
            Map.of('y', "i", 'j', "i", 'x', "kh", 'w', "v");

    private ImportUtil() {
    }

    public record CampusGroup(int id, String name, String faculty) {

        /**
         * Button caption: the faculty disambiguates similarly named groups at a glance.
         */
        public String label() {
            return (faculty == null || faculty.isBlank()) ? name : name + " · " + faculty;
        }
    }

    /**
     * Finds every group whose name matches the query, best match first: exact, then prefix, then
     * substring. If nothing matches, the query is retried with Latin lookalikes folded to Cyrillic.
     */
    public static List<CampusGroup> searchGroups(String query) {
        if (query == null) {
            return List.of();
        }
        List<CampusGroup> matches = match(query, ImportUtil::normalize);
        if (matches.isEmpty()) {
            matches = match(query, ImportUtil::latinize);
        }
        return matches;
    }

    public static Optional<CampusGroup> findGroupById(int groupId) {
        return allGroups().stream().filter(group -> group.id() == groupId).findFirst();
    }

    public static CampusTimetable getScheduleByGroup(int groupId) {
        String response = sendGetRequest(LESSONS_ENDPOINT, Map.of("groupId", String.valueOf(groupId)), "schedule");
        try {
            return MAPPER.readValue(response, CampusTimetable.class);
        } catch (IOException e) {
            throw new CampusApiException("Could not parse the schedule of group " + groupId, "schedule", e);
        }
    }

    /**
     * True when the campus has not published anything for this group - importing it would only wipe
     * whatever the chat already has.
     */
    public static boolean isEmpty(CampusTimetable timetable) {
        return timetable == null
               || (countClasses(timetable.getScheduleFirstWeek()) == 0
                   && countClasses(timetable.getScheduleSecondWeek()) == 0);
    }

    private static int countClasses(List<CampusDay> days) {
        if (days == null) {
            return 0;
        }
        int total = 0;
        for (CampusDay day : days) {
            if (day.getPairs() != null) {
                total += day.getPairs().size();
            }
        }
        return total;
    }

    private static List<CampusGroup> match(String query, UnaryOperator<String> canonicalise) {
        String needle = canonicalise.apply(query);
        if (needle.length() < MIN_QUERY_LENGTH) {
            return List.of();
        }

        List<CampusGroup> exact = new ArrayList<>();
        List<CampusGroup> prefix = new ArrayList<>();
        List<CampusGroup> contains = new ArrayList<>();
        for (CampusGroup group : allGroups()) {
            String name = canonicalise.apply(group.name());
            if (name.equals(needle)) {
                exact.add(group);
            } else if (name.startsWith(needle)) {
                prefix.add(group);
            } else if (name.contains(needle)) {
                contains.add(group);
            }
        }

        Comparator<CampusGroup> byName = Comparator.comparing(CampusGroup::name);
        prefix.sort(byName);
        contains.sort(byName);

        List<CampusGroup> matches = new ArrayList<>(exact.size() + prefix.size() + contains.size());
        matches.addAll(exact);
        matches.addAll(prefix);
        matches.addAll(contains);
        return matches;
    }

    private static String normalize(String value) {
        return value.trim().toLowerCase().replace(" ", "");
    }

    /**
     * Rewrites a name into a Latin form both a Cyrillic and a Latin spelling of it agree on.
     * Only used as a fallback: it deliberately conflates a few letters to be forgiving.
     */
    private static String latinize(String value) {
        String normalized = normalize(value);
        StringBuilder latin = new StringBuilder(normalized.length());
        for (char character : normalized.toCharArray()) {
            String replacement = CYRILLIC_TO_LATIN.get(character);
            if (replacement == null) {
                replacement = LATIN_ALIASES.get(character);
            }
            latin.append(replacement != null ? replacement : character);
        }
        return latin.toString();
    }

    private static List<CampusGroup> allGroups() {
        List<CampusGroup> snapshot = cachedGroups;
        if (snapshot != null && isNotStale()) {
            return snapshot;
        }

        synchronized (CACHE_LOCK) {
            if (cachedGroups != null && isNotStale()) {
                return cachedGroups;
            }
            try {
                List<CampusGroup> fetched = fetchGroups();
                cachedGroups = fetched;
                cachedAt = System.currentTimeMillis();
                log.info("Cached {} KPI Campus groups", fetched.size());
                return fetched;
            } catch (CampusApiException e) {
                if (cachedGroups != null) {
                    log.warn("Serving a stale KPI Campus group list, refresh failed: {}", e.getMessage());
                    return cachedGroups;
                }
                throw e;
            }
        }
    }

    private static boolean isNotStale() {
        return System.currentTimeMillis() - cachedAt < CACHE_TTL_MS;
    }

    private static List<CampusGroup> fetchGroups() {
        String response = sendGetRequest(GROUPS_ENDPOINT, Map.of(), "groups");
        try {
            JSONArray array = new JSONArray(response);
            List<CampusGroup> groups = new ArrayList<>(array.length());
            for (int i = 0; i < array.length(); i++) {
                JSONObject entry = array.getJSONObject(i);
                String name = entry.optString("name", "");
                int id = entry.optInt("id", -1);
                if (id < 0 || name.isBlank()) {
                    continue;
                }
                groups.add(new CampusGroup(id, name, entry.optString("faculty", null)));
            }
            if (groups.isEmpty()) {
                throw new CampusApiException("KPI Campus returned no groups at all", "groups");
            }
            return groups;
        } catch (CampusApiException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new CampusApiException("Could not parse the KPI Campus group list", "groups", e);
        }
    }

    private static String sendGetRequest(String endpoint, Map<String, String> params, String operation) {
        String target = buildUrl(endpoint, params);
        try {
            for (int hop = 0; hop <= MAX_REDIRECTS; hop++) {
                HttpURLConnection connection = (HttpURLConnection) URI.create(target).toURL().openConnection();
                try {
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setRequestProperty("User-Agent", "Lumios-Bot");
                    connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
                    connection.setReadTimeout(READ_TIMEOUT_MS);
                    // /schedule/groups answers with a 302 to a CDN dump; HttpURLConnection refuses to
                    // follow redirects across hosts on its own, so they are followed explicitly.
                    connection.setInstanceFollowRedirects(false);

                    int status = connection.getResponseCode();
                    if (isRedirect(status)) {
                        String location = connection.getHeaderField("Location");
                        if (location == null || location.isBlank()) {
                            throw new CampusApiException(status + " redirect without a Location header", operation);
                        }
                        target = URI.create(target).resolve(location).toString();
                        continue;
                    }
                    if (status != HttpURLConnection.HTTP_OK) {
                        throw new CampusApiException(
                                "KPI Campus answered " + status + ": " + readBody(connection.getErrorStream()), operation);
                    }

                    String body = readBody(connection.getInputStream()).trim();
                    if (body.isEmpty() || (!body.startsWith("{") && !body.startsWith("["))) {
                        throw new CampusApiException("KPI Campus answered with non-JSON content", operation);
                    }
                    return body;
                } finally {
                    connection.disconnect();
                }
            }
            throw new CampusApiException("Too many redirects from " + endpoint, operation);
        } catch (IOException e) {
            throw new CampusApiException("KPI Campus is unreachable: " + e.getMessage(), operation, e);
        }
    }

    private static boolean isRedirect(int status) {
        return status == HttpURLConnection.HTTP_MOVED_PERM
               || status == HttpURLConnection.HTTP_MOVED_TEMP
               || status == HttpURLConnection.HTTP_SEE_OTHER
               || status == 307
               || status == 308;
    }

    private static String buildUrl(String endpoint, Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return endpoint;
        }
        StringJoiner query = new StringJoiner("&");
        params.forEach((key, value) -> query.add(
                URLEncoder.encode(key, StandardCharsets.UTF_8) + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8)));
        return endpoint + "?" + query;
    }

    /**
     * Reads the stream as UTF-8 - the platform default would mangle Cyrillic group names on Windows.
     */
    private static String readBody(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        try (InputStream input = stream) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[8192];
            int read;
            while ((read = input.read(chunk)) != -1) {
                buffer.write(chunk, 0, read);
            }
            return buffer.toString(StandardCharsets.UTF_8);
        }
    }
}
