package vlsurhai.ganokbot.service;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vlsurhai.ganokbot.common.model.cache.CachedChat;
import vlsurhai.ganokbot.common.model.jpa.Chat;
import vlsurhai.ganokbot.common.model.jpa.ChatSetting;
import vlsurhai.ganokbot.repository.ChatRepository;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Slf4j
@Service
public class ChatService {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private Cache<Long, CachedChat> chatsCache;

    public CachedChat peekChat(Long chatId) {
        return chatsCache.getIfPresent(chatId);
    }

    public CachedChat getChat(Long chatId) {
        return chatsCache.get(chatId, this::loadChat);
    }

    private CachedChat loadChat(Long chatId) {
        return chatRepository.findById(chatId)
                .map(this::mapToCachedChat)
                .orElse(null);
    }

    @Transactional
    public void ensureChat(Long chatId, String type) {
        CachedChat cachedChat = chatsCache.get(chatId, this::loadChat);

        if (cachedChat == null) {
            Chat chat = chatRepository.save(new Chat(chatId, type));
            cachedChat = mapToCachedChat(chat);

            chatsCache.put(chatId, cachedChat);
            log.info("Chat({}) of type '{}' has been created", chatId, type);
        }
    }

    @Transactional
    public void cleanupChat(Long chatId) {
        chatRepository.deleteById(chatId);
        chatsCache.invalidate(chatId);
    }

    @Transactional
    public void updateChatSettings(Long chatId, Map<String, ChatSetting> newSettings) {
        Chat chat = chatRepository.findById(chatId).orElseThrow();
        Iterator<ChatSetting> it = chat.getSettings().iterator();

        while (it.hasNext()) {
            ChatSetting current = it.next();
            ChatSetting newSetting = newSettings.remove(current.getKey());

            if (newSetting != null) {
                current.setValue(newSetting.getValue());
            } else {
                it.remove();
            }
        }
        for (ChatSetting remaining : newSettings.values()) {
            remaining.setChatId(chatId);
            chat.getSettings().add(remaining);
        }
        chatRepository.save(chat);
        chatsCache.put(chatId, mapToCachedChat(chat));
        log.info("Registered {} settings for chat({})", chat.getSettings().size(), chatId);
    }

    public boolean isSettingEnabled(Long chatId, String settingKey) {
        CachedChat cachedChat = getChat(chatId);
        return cachedChat != null && Boolean.parseBoolean(cachedChat.settings().get(settingKey));
    }

    private CachedChat mapToCachedChat(Chat chat) {
        if (chat == null) {
            return null;
        }
        Map<String, String> cachedSettings = new HashMap<>();
        for (ChatSetting setting : chat.getSettings()) {
            cachedSettings.put(setting.getKey(), setting.getValue());
        }
        return new CachedChat(chat.getChatId(), chat.getType(), cachedSettings
        );
    }
}

