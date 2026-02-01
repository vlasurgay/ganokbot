package vlsurhai.ganokbot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import vlsurhai.ganokbot.common.model.jpa.ChatMember;

import java.util.List;

@Service
public class ChatSyncService {

    @Autowired
    private UserService userService;

    @Autowired
    private ChatMemberService chatMemberService;

    @Transactional
    public void upsertChatAdministrators(Long chatId,
                                         List<org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember> tgAdmins) {

        for (org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember tgAdmin : tgAdmins) {
            long userId = tgAdmin.getUser().getId();
            boolean isBot = tgAdmin.getUser().getIsBot();

            upsertChatMember(userId, chatId, isBot, tgAdmin.getStatus());
        }

        List<Long> keepIds = tgAdmins.stream()
                .map(a -> a.getUser().getId())
                .toList();

        if (!CollectionUtils.isEmpty(keepIds)) {
            chatMemberService.downgradeAdminsNotIn(chatId, keepIds);
        }
    }


    public ChatMember upsertChatMember(Long userId, Long chatId, boolean isBot, String status) {
        userService.getOrCreateUser(userId, isBot);
        return chatMemberService.createOrUpdateChatMember(userId, chatId, status);
    }
}
