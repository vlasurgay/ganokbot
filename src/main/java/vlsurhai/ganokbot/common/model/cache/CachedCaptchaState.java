package vlsurhai.ganokbot.common.model.cache;

import java.time.LocalDateTime;

public record CachedCaptchaState(
        Long id,
        Long userId,
        Long chatId,
        String captchaText,
        LocalDateTime expiresAt,
        int attempts
) { }
