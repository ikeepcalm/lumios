package dev.ua.ikeepcalm.lumios.telegram;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.ua.ikeepcalm.lumios.telegram.modules.HandlerParent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {

    private static final Logger log = LoggerFactory.getLogger(UpdateConsumer.class);
    private final List<HandlerParent> handlerParentList;
    private final Cache<Long, UserActivity> userActivityCache;

    public UpdateConsumer(List<HandlerParent> handlerParentList) {
        this.handlerParentList = handlerParentList;
        this.userActivityCache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build();
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage()) {
            long userId = update.getMessage().getFrom().getId();
            long currentTime = System.currentTimeMillis();

            UserActivity userActivity = userActivityCache.get(userId, k -> new UserActivity());
            if (userActivity.isRateLimited(currentTime)) {
                log.info("User {} is rate limited", userId);
                return;
            }

            userActivity.recordRequest(currentTime);
            userActivityCache.put(userId, userActivity);
        }

        for (HandlerParent handlerParent : this.handlerParentList) {
            if (handlerParent.supports(update)) {
                handlerParent.dispatchUpdate(update);
            }
        }
    }

    private static class UserActivity {
        private long lastRequestTime;
        private int requestCount;
        private final long RATE_LIMIT_INTERVAL_MS = TimeUnit.SECONDS.toMillis(5);
        private final int MAX_REQUESTS_PER_INTERVAL = 5;

        boolean isRateLimited(long currentTime) {
            if (currentTime - lastRequestTime > RATE_LIMIT_INTERVAL_MS) {
                lastRequestTime = currentTime;
                requestCount = 1;
                return false;
            } else {
                requestCount++;
                return requestCount > MAX_REQUESTS_PER_INTERVAL;
            }
        }

        void recordRequest(long currentTime) {
            if (currentTime - lastRequestTime > RATE_LIMIT_INTERVAL_MS) {
                lastRequestTime = currentTime;
                requestCount = 1;
            } else {
                requestCount++;
            }
        }
    }

}

