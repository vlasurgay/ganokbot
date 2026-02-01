package vlsurhai.ganokbot.service;

import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import vlsurhai.ganokbot.common.model.SettingsState;

@Service
public class ChatSettingService {

    @Autowired
    private Cache<Long, SettingsState> settingsStateCache;

    public void createState(Long userId, Long chatId) {
        SettingsState state = new SettingsState(userId, chatId);
        settingsStateCache.put(userId, state);
    }

    public SettingsState getState(Long userId) {
        return settingsStateCache.getIfPresent(userId);
    }

    public SettingsState getState(Update update) {
        if (update.hasCallbackQuery()) {
            return getState(update.getCallbackQuery().getFrom().getId());
        }
        if (update.hasMessage()) {
            return getState(update.getMessage().getFrom().getId());
        }
        return null;
    }

    public void deleteState(Long userId) {
        settingsStateCache.invalidate(userId);
    }

    public boolean isBeingSetUp(Update update) {
        if (update.hasCallbackQuery()) {
            return settingsStateCache.getIfPresent(update.getCallbackQuery().getFrom().getId()) != null;
        }
        if (update.hasMessage()) {
            return settingsStateCache.getIfPresent(update.getMessage().getFrom().getId()) != null;
        }
        return false;
    }
}
