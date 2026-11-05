package dev.ua.ikeepcalm.lumios.telegram.utils;

import dev.ua.ikeepcalm.lumios.database.entities.queue.MixedQueue;
import dev.ua.ikeepcalm.lumios.database.entities.queue.MixedUser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QueueUpdateUtilTest {

    @Test
    void mixedQueueListsMembersAlphabeticallyWithoutPositions() {
        MixedQueue queue = new MixedQueue("Lab 3");
        queue.getContents().add(user("zoe", "Zoe"));
        queue.getContents().add(user("adam", "adam"));
        queue.getContents().add(user("bob", "Bob"));

        String text = QueueUpdateUtil.updateMessage(1L, queue).getText();

        assertEquals(">>> Lab 3 <<<\n\n• adam (@adam)\n• Bob (@bob)\n• Zoe (@zoe)\n", text);
        assertEquals("zoe", queue.getContents().getFirst().getUsername(), "rendering must not reorder the queue itself");
    }

    private static MixedUser user(String username, String name) {
        MixedUser user = new MixedUser();
        user.setUsername(username);
        user.setName(name);
        return user;
    }
}
