package dev.ua.ikeepcalm.lumios.telegram.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * The single clock the timetable feature runs on.
 * <p>
 * Classes are scheduled in Kyiv local time, but the container has no {@code TZ} set, so the JVM
 * default is UTC. Anything that mixes the two silently drifts by two or three hours: cron windows
 * miss early classes, and {@code LocalDate.now()} returns yesterday between midnight and 03:00.
 * Every timetable calculation must go through here, and every {@code @Scheduled} that reasons about
 * class times must pass {@link #ZONE_ID} as its {@code zone}.
 */
public final class TimetableClock {

    /**
     * Kept as a literal so it can be used in {@code @Scheduled(zone = ...)}, which needs a constant.
     * Beware: Spring resolves that attribute with {@code TimeZone.getTimeZone}, which falls back to
     * GMT without complaint on an unknown id - so this value must stay a valid zone id.
     */
    public static final String ZONE_ID = "Europe/Kyiv";

    public static final ZoneId ZONE = ZoneId.of(ZONE_ID);

    private TimetableClock() {
    }

    public static LocalDate today() {
        return LocalDate.now(ZONE);
    }

    public static LocalTime now() {
        return LocalTime.now(ZONE);
    }

    public static LocalDateTime nowDateTime() {
        return LocalDateTime.now(ZONE);
    }

    /**
     * Current time truncated to the minute. Class start times are always whole minutes, so this is
     * what schedules should be matched against - it makes "did this minute already fire" exact.
     */
    public static LocalTime currentMinute() {
        return now().withSecond(0).withNano(0);
    }
}
