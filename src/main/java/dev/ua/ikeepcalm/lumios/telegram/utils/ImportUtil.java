package dev.ua.ikeepcalm.lumios.telegram.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.campus.CampusTimetable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

public class ImportUtil {

    public static Map<String, String> getGroupsByFilter(String filter) throws RuntimeException {
        try {
            String response = sendGetRequest("https://api.campus.kpi.ua/group/find", Map.of("name", filter));

            if (response == null || response.trim().isEmpty()) {
                throw new RuntimeException("Empty response from API");
            }
            
            String trimmedResponse = response.trim();
            if (!trimmedResponse.startsWith("{") && !trimmedResponse.startsWith("[")) {
                throw new RuntimeException("Invalid JSON response: " + trimmedResponse.substring(0, Math.min(100, trimmedResponse.length())));
            }
            
            JSONArray groups = new JSONArray(response);
            Map<String, String> groupNames = new HashMap<>();
            for (int i = 0; i < groups.length(); i++) {
                JSONObject group = groups.getJSONObject(i);
                String groupName = group.getString("name");
                String groupId = group.getString("id");
                groupNames.put(groupId, groupName);
            }

            return groupNames;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get groups by filter", e);
        }
    }

    public static CampusTimetable getScheduleByGroup(String groupId) throws RuntimeException {
        try {
            String response = sendGetRequest("https://api.campus.kpi.ua/schedule/lessons", Map.of("groupId", groupId));
            
            if (response == null || response.trim().isEmpty()) {
                throw new RuntimeException("Empty response from API");
            }
            
            String trimmedResponse = response.trim();
            if (!trimmedResponse.startsWith("{") && !trimmedResponse.startsWith("[")) {
                throw new RuntimeException("Invalid JSON response: " + trimmedResponse.substring(0, Math.min(100, trimmedResponse.length())));
            }

            ObjectMapper mapper = new ObjectMapper();
            if (trimmedResponse.startsWith("{")) {
                JSONObject jsonResponse = new JSONObject(response);
                
                if (jsonResponse.has("data")) {
                    JSONObject data = jsonResponse.getJSONObject("data");
                    return mapper.readValue(data.toString(), CampusTimetable.class);
                } else {
                    return mapper.readValue(response, CampusTimetable.class);
                }
            } else {
                return mapper.readValue(response, CampusTimetable.class);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get schedule by group", e);
        }
    }

    private static String sendGetRequest(String endpoint, Map<String, String> params) throws Exception {
        StringBuilder urlWithParams = new StringBuilder(endpoint);

        if (params != null && !params.isEmpty()) {
            urlWithParams.append("?");
            StringJoiner sj = new StringJoiner("&");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "=" + URLEncoder.encode(entry.getValue(), "UTF-8"));
            }
            urlWithParams.append(sj);
        }

        URL url = new URL(urlWithParams.toString());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");

        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            connection.disconnect();
            return content.toString();
        } else {
            // Try to read error response body for better debugging
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            StringBuilder errorContent = new StringBuilder();
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                errorContent.append(errorLine);
            }
            errorReader.close();
            connection.disconnect();
            throw new RuntimeException("HTTP GET Request Failed with Error code: " + responseCode + 
                                     ", Error response: " + errorContent.toString());
        }
    }

    public static ReplyKeyboard createGroupsKeyboard(Map<String, String> groups) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        int maxButtons = Math.min(groups.size(), 9);

        int count = 0;
        for (Map.Entry<String, String> entry : groups.entrySet()) {
            if (count >= maxButtons) break;
            InlineKeyboardButton button = new InlineKeyboardButton(entry.getValue());
            button.setCallbackData("import#" + entry.getKey());
            buttons.add(button);
            count++;
        }

        int rows, columns;
        if (buttons.size() <= 6) {
            rows = 3;
            columns = 2;
        } else {
            rows = 3;
            columns = 3;
        }

        for (int i = 0; i < rows; i++) {
            InlineKeyboardRow row = new InlineKeyboardRow();
            for (int j = 0; j < columns; j++) {
                int index = i * columns + j;
                if (index < buttons.size()) {
                    row.add(buttons.get(index));
                }
            }
            keyboard.add(row);
        }

        return new InlineKeyboardMarkup(keyboard);
    }
}
