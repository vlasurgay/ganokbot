package vlsurhai.ganokbot.reply.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;
import org.telegram.telegrambots.meta.api.objects.MemberStatus;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import vlsurhai.ganokbot.reply.AbstractReply;
import vlsurhai.ganokbot.service.ChatService;
import vlsurhai.ganokbot.service.ChatSyncService;
import vlsurhai.ganokbot.service.TelegramApiService;

import java.util.List;

@Slf4j
@Order(0)
@Component
public class BotPromotedReply extends AbstractReply {

    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatSyncService chatSyncService;

    @Autowired
    private TelegramApiService telegramApiService;

    @Override
    protected void getAction(Update update, BaseAbilityBot bot) {
        ChatMemberUpdated myChatMember = update.getMyChatMember();
        Long chatId = myChatMember.getChat().getId();
        try {
            chatService.ensureChat(chatId, myChatMember.getChat().getType());
            proceedMyChatMember(myChatMember, (AbilityBot) bot);

            log.info("Bot has been promoted in chat({})", chatId);
        } catch (TelegramApiException e) {
            log.warn("Failed to add myself to a chat(id={}): {}", chatId, e.getMessage(), e);
        }
    }

    @Override
    protected boolean getCondition(Update update) {
        if (update.hasMyChatMember() && update.getMyChatMember().getNewChatMember() != null) {
            String status = update.getMyChatMember().getNewChatMember().getStatus();
            return status.equals(MemberStatus.MEMBER) || status.equals(MemberStatus.ADMINISTRATOR) || status.equals(MemberStatus.CREATOR);
        }
        return false;
    }

    private void proceedMyChatMember(ChatMemberUpdated myChatMember, AbilityBot bot) throws TelegramApiException {
        org.telegram.telegrambots.meta.api.objects.Chat chat = myChatMember.getChat();
        String status = myChatMember.getNewChatMember().getStatus();

        chatSyncService.upsertChatMember(bot.getMe().getId(), chat.getId(), true, status);

        if (isGroup(chat) && isAdministrator(status)) {
            List<ChatMember> tgChatMembers = telegramApiService.getChatAdministrators(chat.getId(), bot);
            chatSyncService.upsertChatAdministrators(chat.getId(), tgChatMembers);
        }
    }

    private boolean isGroup(Chat chat) {
        return chat.isGroupChat() || chat.isSuperGroupChat();
    }

    private boolean isAdministrator(String status) {
        return status.equals(MemberStatus.ADMINISTRATOR) || status.equals(MemberStatus.CREATOR);
    }
}
