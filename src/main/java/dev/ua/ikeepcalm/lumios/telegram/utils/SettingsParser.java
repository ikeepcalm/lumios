package dev.ua.ikeepcalm.lumios.telegram.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import dev.ua.ikeepcalm.lumios.database.entities.queue.wrappers.QueueWrapper;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.wrappers.SettingsWrapper;

public class SettingsParser {

    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
    }

    public static SettingsWrapper parseSettings(String json) throws JsonProcessingException {
        ObjectReader objectReader = objectMapper.readerFor(SettingsWrapper.class);
        return objectReader.readValue(json);
    }

}

