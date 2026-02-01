package vlsurhai.ganokbot.common.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vlsurhai.ganokbot.common.model.SettingsState;
import vlsurhai.ganokbot.common.model.cache.CachedCaptchaState;
import vlsurhai.ganokbot.common.model.cache.CachedChat;

import java.time.Instant;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class CacheConfig {

    @Bean
    public Cache<Long, ConcurrentLinkedDeque<Instant>> userMessageTimestamps(@Value("${spam.time-window.seconds:10}") int spamTimeWindow) {
        return Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterAccess(spamTimeWindow, TimeUnit.SECONDS)
                .build();
    }

    @Bean
    public Cache<Long, CachedChat> chatsCache() {
        return Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(6, TimeUnit.HOURS)
                .build();
    }

    @Bean
    public Cache<Long, CachedCaptchaState> captchaStateCache() {
        return Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterAccess(1, TimeUnit.DAYS)
                .build();
    }

    @Bean
    public Cache<Long, SettingsState> settingsStateCache() {
        return Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterAccess(1, TimeUnit.HOURS)
                .build();
    }
}
