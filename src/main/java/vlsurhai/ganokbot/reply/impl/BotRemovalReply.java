package vlsurhai.ganokbot.reply.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;
import org.telegram.telegrambots.meta.api.objects.MemberStatus;
import org.telegram.telegrambots.meta.api.objects.Update;
import vlsurhai.ganokbot.reply.AbstractReply;
import vlsurhai.ganokbot.service.ChatMemberService;
import vlsurhai.ganokbot.service.ChatService;

@Slf4j
@Component
public class BotRemovalReply extends AbstractReply {

    @Autowired
    private ChatMemberService chatMemberService;

    @Autowired
    private ChatService chatService;

    @Override
    protected void getAction(Update update, BaseAbilityBot bot) {
        ChatMemberUpdated chatMember = update.getMyChatMember();
        Long chatId = chatMember.getChat().getId();

        chatMemberService.deleteChatMembersByChatId(chatId);
        chatService.cleanupChat(chatId);
        log.info("Bot has been removed from chat({})", chatId);

    }

    @Override
    protected boolean getCondition(Update update) {
        if (update.hasMyChatMember() && update.getMyChatMember().getNewChatMember() != null) {
            String status = update.getMyChatMember().getNewChatMember().getStatus();
            return status.equals(MemberStatus.KICKED) || status.equals(MemberStatus.LEFT);
        }
        return false;
    }
}
