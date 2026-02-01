package vlsurhai.ganokbot.reply.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.telegrambots.meta.api.objects.Chat;
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
@Order(1)
@Component
public class SyncChatReply extends AbstractReply {

    @Autowired
    private ChatSyncService chatSyncService;
    
    @Autowired
    private ChatService chatService;

    @Autowired
    private TelegramApiService telegramApiService;


    @Override
    protected void getAction(Update update, BaseAbilityBot bot) {
        Chat chat = extractChat(update);
        try {
            chatService.ensureChat(chat.getId(), chat.getType());
            proceedChatMaintenance(chat, (AbilityBot) bot);

            log.info("Bot has been synced with chat({})", chat.getId());
        } catch (TelegramApiException e) {
            log.warn("Failed to sync with chat(id={}): {}", chat.getId(), e.getMessage(), e);
        }
    }

    @Override
    protected boolean getCondition(Update update) {
        if (update.hasMyChatMember()) {
            return false;
        }
        Chat chat = extractChat(update);
        if (chat == null) {
            return false;
        }
        return chatService.peekChat(chat.getId()) == null;
    }

    private void proceedChatMaintenance(Chat chat, AbilityBot bot) throws TelegramApiException {
        Long chatId = chat.getId();

        ChatMember selfMember = telegramApiService.getChatMember(bot.getMe().getId(), chatId, bot);

        if (selfMember == null) {
            return;
        }
        chatSyncService.upsertChatMember(selfMember.getUser().getId(), chatId, true, selfMember.getStatus());

        if (isGroup(chat) && isAdministrator(selfMember)) {
            List<ChatMember> tgChatMembers = telegramApiService.getChatAdministrators(chat.getId(), bot);
            chatSyncService.upsertChatAdministrators(chat.getId(), tgChatMembers);
        }
    }

    private Chat extractChat(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getChat();
        }
        else if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getChat();
        }
        else if (update.hasChatMember()) {
            return update.getChatMember().getChat();
        }
        else if (update.hasEditedMessage()) {
            return update.getEditedMessage().getChat();
        }
        return null;
    }

    private boolean isGroup(Chat chat) {
        return chat.isGroupChat() || chat.isSuperGroupChat();
    }

    private boolean isAdministrator(ChatMember chatMember) {
        String status = chatMember.getStatus();
        return status.equals(MemberStatus.ADMINISTRATOR) || status.equals(MemberStatus.CREATOR);
    }
}
