package vlsurhai.ganokbot.ability.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import vlsurhai.ganokbot.ability.AbstractAbility;
import vlsurhai.ganokbot.common.i18n.MessageResolver;
import vlsurhai.ganokbot.common.model.cache.CachedChat;
import vlsurhai.ganokbot.service.ChatService;
import vlsurhai.ganokbot.service.TelegramApiService;

import static vlsurhai.ganokbot.common.Constants.*;
import static vlsurhai.ganokbot.common.settings.SettingKey.RULES;

@Slf4j
@Component
public class RulesAbility extends AbstractAbility {

    @Autowired
    private MessageResolver resolver;

    @Autowired
    private ChatService chatService;

    @Autowired
    private TelegramApiService telegramApiService;

    @Override
    public String getAbilityName() {
        return RULES_ABILITY;
    }

    @Override
    public String getInfo() {
        return resolver.getMessage(RULES_INFO);
    }

    @Override
    protected void getAction(MessageContext ctx) {
        Message message = ctx.update().getMessage();

        CachedChat chat = chatService.getChat(ctx.chatId());

        String defaultCaption = resolver.getMessage(NO_RULES_SPECIFIED_EXCEPTION_MESSAGE);
        String response = chat.settings().getOrDefault(RULES.getKey(), defaultCaption);

        try {
            telegramApiService.sendMessageReply(
                    message.getChatId(), message.getMessageId(), response, (AbilityBot) ctx.bot()
            );
        } catch (TelegramApiException e) {
            log.warn("Exception while getting chat({}) rules: {}", message.getChatId(), e.getMessage(), e);
        }
    }
}
