package vlsurhai.ganokbot.service;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vlsurhai.ganokbot.common.CaptchaGenerator;
import vlsurhai.ganokbot.common.model.CaptchaPayload;
import vlsurhai.ganokbot.common.model.cache.CachedCaptchaState;
import vlsurhai.ganokbot.common.model.jpa.CaptchaState;
import vlsurhai.ganokbot.repository.CaptchaStateRepository;

import java.awt.*;
import java.io.IOException;
import java.time.LocalDateTime;

import static vlsurhai.ganokbot.common.utils.Utils.encode;

@Slf4j
@Service
public class CaptchaService {


    @Autowired
    private CaptchaGenerator captchaGenerator;

    @Autowired
    private CaptchaStateRepository captchaStateRepository;

    @Autowired
    private Cache<Long, CachedCaptchaState> captchaStateCache;

    @Transactional
    public CaptchaPayload createCaptcha(Long userId, Long chatId, LocalDateTime expiresAt) throws IOException, FontFormatException {
        CaptchaPayload payload = captchaGenerator.generate();
        CachedCaptchaState existing = getCaptchaState(userId, chatId);
        int attempts = existing != null ? existing.attempts() : 0;

        saveCaptchaState(userId, chatId, payload.text(), expiresAt, attempts);
        return payload;
    }

    public boolean hasActiveCaptcha(Long userId, Long chatId) {
        CachedCaptchaState state = getCaptchaState(userId, chatId);
        return state != null && state.expiresAt().isAfter(LocalDateTime.now());
    }

    public CachedCaptchaState getCaptchaState(Long targetUserId, Long chatId) {
        Long hash = encode(targetUserId, chatId);
        return captchaStateCache.get(hash, k -> loadCaptchaState(targetUserId, chatId));
    }

    private CachedCaptchaState loadCaptchaState(Long targetUserId, Long chatId) {
        CaptchaState captchaState = captchaStateRepository.findByUserIdAndChatId(targetUserId, chatId);
        return captchaState != null ? mapToCachedCaptcha(captchaState) : null;
    }

    @Transactional
    private CachedCaptchaState saveCaptchaState(Long userId, Long chatId, String text, LocalDateTime expiresAt, int attempts) {
        CaptchaState state = captchaStateRepository.findByUserIdAndChatId(userId, chatId);
        if (state == null) {
            state = new CaptchaState(userId, chatId, expiresAt, text, attempts);
        } else {
            state.setCaptchaText(text);
            state.setAttempts(attempts);
            state.setExpiresAt(expiresAt);
        }
        CaptchaState saved = captchaStateRepository.save(state);
        CachedCaptchaState cachedCaptchaState = mapToCachedCaptcha(saved);
        captchaStateCache.put(encode(userId, chatId), cachedCaptchaState);
        return cachedCaptchaState;
    }

    @Transactional
    public CachedCaptchaState incrementAttempts(Long userId, Long chatId) {
        CachedCaptchaState state = getCaptchaState(userId, chatId);
        if (state == null) {
            return null;
        }
        int newAttempts = state.attempts() + 1;
        return saveCaptchaState(userId, chatId, state.captchaText(), state.expiresAt(), newAttempts);
    }

    @Transactional
    public void cleanupCaptchaState(Long userId, Long chatId) {
        captchaStateRepository.deleteByUserIdAndChatId(userId, chatId);
        Long hash = encode(userId, chatId);
        captchaStateCache.invalidate(hash);

    }

    private CachedCaptchaState mapToCachedCaptcha(CaptchaState entity) {
        return new CachedCaptchaState(
                entity.getId(),
                entity.getUserId(),
                entity.getChatId(),
                entity.getCaptchaText(),
                entity.getExpiresAt(),
                entity.getAttempts()
        );
    }
}
