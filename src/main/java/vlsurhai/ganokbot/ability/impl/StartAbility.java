package vlsurhai.ganokbot.ability.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.MessageContext;
import vlsurhai.ganokbot.ability.AbstractAbility;
import vlsurhai.ganokbot.common.i18n.MessageResolver;
import vlsurhai.ganokbot.service.TelegramApiService;

import static vlsurhai.ganokbot.common.Constants.*;

@Slf4j
@Component
public class StartAbility extends AbstractAbility {

    @Autowired
    private MessageResolver resolver;

    @Autowired
    private TelegramApiService telegramApiService;

    @Override
    public String getInfo() {
        return resolver.getMessage(START_INFO);
    }

    @Override
    public String getAbilityName() {
        return START_ABILITY;
    }

    @Override
    public Locality getLocality() {
        return Locality.USER;
    }

    @Override
    public boolean isShown() {
        return false;
    }

    @Override
    protected void getAction(MessageContext ctx) {
        String message = resolver.getMessage(START_MESSAGE);
        try {
            telegramApiService.sendMessage(ctx.chatId(), message, (AbilityBot) ctx.bot());
        } catch (Exception e) {
            log.error("Failed to send start message to chat(id={}): {}", ctx.chatId(), e.getMessage(), e);
        }
    }
}
