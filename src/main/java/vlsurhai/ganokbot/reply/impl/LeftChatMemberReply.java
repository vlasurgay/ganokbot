package vlsurhai.ganokbot.reply.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import vlsurhai.ganokbot.reply.AbstractReply;
import vlsurhai.ganokbot.service.ChatMemberService;

@Slf4j
@Component
public class LeftChatMemberReply extends AbstractReply {

    @Autowired
    private ChatMemberService chatMemberService;

    @Override
    protected void getAction(Update update, BaseAbilityBot bot) {
        User user = update.getMessage().getLeftChatMember();
        Chat chat = update.getMessage().getChat();

        chatMemberService.deleteChatMember(user.getId(), chat.getId());
        log.warn("User({}) left or was kicked from chat({})", user.getId(), chat.getId());
    }

    @Override
    protected boolean getCondition(Update update) {
        return update.hasMessage() && update.getMessage().getLeftChatMember() != null;
    }
}
