package dev.ua.ikeepcalm.lumios.database.entities.records.wrappers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.ua.ikeepcalm.lumios.database.entities.records.MessageRecord;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageWrapper {

    @JsonProperty("username")
   private String username;

    @JsonProperty("messages")
    private long messagesCount;

    @JsonProperty("dayMessages")
    private HashMap<LocalDate, HashMap<Integer, Integer>> hourlyMessages;

    @JsonProperty("dailyMessages")
    private HashMap<LocalDate, Integer> dailyMessages;

    @JsonProperty("averageDailyMessages")
    private HashMap<LocalDateTime, Integer> averageDailyMessages;

    public MessageWrapper() {
        this.hourlyMessages = new HashMap<>();
        this.dailyMessages = new HashMap<>();
        this.averageDailyMessages = new HashMap<>();
    }

    public static List<MessageWrapper> wrapperList( List<MessageRecord> messageRecords) {
        HashMap<String, MessageWrapper> wrappersMap = new HashMap<>();

        for (MessageRecord record : messageRecords) {
            String user = record.getUser().getUsername();
            MessageWrapper wrapper = wrappersMap.getOrDefault(user, new MessageWrapper());
            wrapper.setUsername(user);
            wrapper.setMessagesCount(wrapper.getMessagesCount() + 1);

            LocalDate date = record.getDate().toLocalDate();
            int hour = record.getDate().getHour();

            HashMap<Integer, Integer> hourly = wrapper.getHourlyMessages().getOrDefault(date, new HashMap<>());
            hourly.put(hour, hourly.getOrDefault(hour, 0) + 1);
            wrapper.getHourlyMessages().put(date, hourly);

            wrapper.getDailyMessages().put(date, wrapper.getDailyMessages().getOrDefault(date, 0) + 1);

            wrappersMap.put(user, wrapper);
        }

        for (MessageWrapper wrapper : wrappersMap.values()) {
            for (Map.Entry<LocalDate, HashMap<Integer, Integer>> entry : wrapper.getHourlyMessages().entrySet()) {
                LocalDate date = entry.getKey();
                HashMap<Integer, Integer> hourly = entry.getValue();
                int sum = hourly.values().stream().mapToInt(Integer::intValue).sum();
                int average = sum / hourly.size();
                wrapper.getAverageDailyMessages().put(LocalDateTime.of(date, LocalTime.MIDNIGHT), average);
            }
        }

        return new ArrayList<>(wrappersMap.values());
    }
}
