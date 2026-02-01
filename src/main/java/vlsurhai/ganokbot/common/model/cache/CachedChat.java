package vlsurhai.ganokbot.common.model.cache;

import java.time.LocalDateTime;
import java.util.Map;

public record CachedChat(
        Long chatId,
        String type,
        Map<String, String> settings
) { }