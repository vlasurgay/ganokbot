package vlsurhai.ganokbot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.ChatPermissions;
import vlsurhai.ganokbot.common.mapper.ObjectMapper;
import vlsurhai.ganokbot.repository.ChatPermissionsRepository;

@Service
public class ChatPermissionsService {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ChatPermissionsRepository permissionsRepository;

    @Transactional
    public void freezeChatPermissions(Long chatId, ChatPermissions tgPermissions) {
        vlsurhai.ganokbot.common.model.jpa.ChatPermissions permissions = permissionsRepository.findByChatId(chatId);

        mapper.mapChatPermissions(tgPermissions, permissions);
        permissions.setIsFrozen(true);

        permissionsRepository.save(permissions);

    }

    @Transactional
    public org.telegram.telegrambots.meta.api.objects.ChatPermissions unfreezeChatPermissions(Long chatId) {
        ChatPermissions tgPermissions = new ChatPermissions();
        vlsurhai.ganokbot.common.model.jpa.ChatPermissions permissions = permissionsRepository.findByChatId(chatId);

        mapper.mapChatPermissions(permissions, tgPermissions);

        permissions.setIsFrozen(false);
        permissionsRepository.save(permissions);

        return tgPermissions;
    }

    public ChatPermissions mutedPermissions()  {
        return mapper.createFrozenPermissions();
    }

    public boolean isFrozen(Long chatId) {
        return permissionsRepository.isFrozenByChatId(chatId);
    }
}
