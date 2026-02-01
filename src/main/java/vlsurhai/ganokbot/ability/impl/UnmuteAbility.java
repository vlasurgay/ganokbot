package vlsurhai.ganokbot.ability.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import vlsurhai.ganokbot.ability.AbstractAbility;
import vlsurhai.ganokbot.common.i18n.MessageResolver;
import vlsurhai.ganokbot.service.TelegramApiService;

import static vlsurhai.ganokbot.common.Constants.*;
import static vlsurhai.ganokbot.common.utils.Utils.getHtmlMention;

@Slf4j
@Component
public class UnmuteAbility extends AbstractAbility {

    @Autowired
    private MessageResolver resolver;

    @Autowired
    private TelegramApiService telegramApiService;

    @Override
    public String getAbilityName() {
        return UNMUTE_ABILITY;
    }

    @Override
    public String getInfo() { return resolver.getMessage(UNMUTE_INFO); }

    @Override
    public Locality getLocality() {
        return Locality.GROUP;
    }

    @Override
    public Privacy getPrivacy() {
        return Privacy.GROUP_ADMIN;
    }

    @Override
    protected void getAction(MessageContext ctx) {
        Message message = ctx.update().getMessage();

        try {
            if (message.getReplyToMessage() == null) {
                String caption = resolver.getMessage(INCORRECT_COMMAND_EXCEPTION_MESSAGE);
                telegramApiService.sendMessageReply(
                        message.getChatId(), message.getMessageId(), caption, (AbilityBot) ctx.bot()
                );
            } else {
                processUnmuteUser(message, (AbilityBot) ctx.bot());
                log.info("User({}) in chat({})  has been unmuted.", message.getReplyToMessage().getFrom().getId(), ctx.chatId());
            }
        } catch (TelegramApiException e) {
            log.error("Failed to unmute user in chat {}: {}", ctx.chatId(), e.getMessage(), e);
        }

    }

    private void processUnmuteUser(Message message, AbilityBot bot) throws TelegramApiException {
        org.telegram.telegrambots.meta.api.objects.Chat fullChat = telegramApiService.getFullChat(message.getChatId(), bot);

        User targetUser = message.getReplyToMessage().getFrom();
        telegramApiService.updateUserPermissions(
                targetUser.getId(), message.getChatId(), null, fullChat.getPermissions(), bot
        );

        String caption = String.format(resolver.getMessage(UNMUTE_MESSAGE), getHtmlMention(targetUser));

        telegramApiService.sendMessageReply(message.getChatId(), message.getMessageId(), caption, bot);
    }
}
