package vlsurhai.ganokbot.ability.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import vlsurhai.ganokbot.ability.AbstractAbility;
import vlsurhai.ganokbot.common.i18n.MessageResolver;
import vlsurhai.ganokbot.service.TelegramApiService;

import java.util.Comparator;
import java.util.List;

import static vlsurhai.ganokbot.common.Constants.*;

@Slf4j
@Component
public class HelpAbility extends AbstractAbility {

    @Autowired
    private List<AbstractAbility> abilities;

    @Autowired
    private MessageResolver resolver;

    @Autowired
    private TelegramApiService telegramApiService;

    @Override
    public String getInfo() {
        return resolver.getMessage(HELP_INFO);
    }

    @Override
    public String getAbilityName() {
        return HELP_ABILITY;
    }

    @Override
    protected void getAction(MessageContext ctx) {
        String message = buildHelpMessage(ctx);
        try {
            telegramApiService.sendMessage(ctx.chatId(), message, (AbilityBot) ctx.bot());
        } catch (TelegramApiException e) {
            log.error("Failed to send help message to chat(id={}): {}", ctx.chatId(), e.getMessage(), e);
        }
    }

    private String buildHelpMessage(MessageContext ctx) {
        Message message = ctx.update().getMessage();
        String header = resolver.getMessage(HELP_HEADER_MESSAGE);

        StringBuilder sb = new StringBuilder(header);
        sb.append("\n");

        if (message.isUserMessage()) {
            List<String> privateAbilities = abilities.stream()
                    .filter(AbstractAbility::isShown)
                    .filter(this::isVisibleInPrivate)
                    .sorted(Comparator.comparing(AbstractAbility::getAbilityName))
                    .map(this::formatAbility).toList();

            if (!privateAbilities.isEmpty()) {
                sb.append(resolver.getMessage(HELP_SECTION_PRIVATE_MESSAGE));
                sb.append("\n");
                privateAbilities.forEach(sb::append);
                sb.append("\n\n");
            }
        }

        if (message.isGroupMessage() || message.isSuperGroupMessage() || message.isUserMessage()) {
            List<String> groupAbilities = abilities.stream()
                    .filter(AbstractAbility::isShown)
                    .filter(this::isVisibleInGroup)
                    .sorted(Comparator.comparing(AbstractAbility::getAbilityName))
                    .map(this::formatAbility).toList();

            if (!groupAbilities.isEmpty()) {
                sb.append(resolver.getMessage(HELP_SECTION_GROUP_MESSAGE));
                sb.append("\n");
                groupAbilities.forEach(sb::append);
            }
        }
        return sb.toString();
    }

    private boolean isVisibleInGroup(AbstractAbility a) {
        return a.getLocality().equals(Locality.GROUP) || a.getLocality().equals(Locality.ALL);
    }

    private boolean isVisibleInPrivate(AbstractAbility a) {
        return a.getLocality().equals(Locality.USER) || a.getLocality().equals(Locality.ALL);
    }

    private String formatAbility(AbstractAbility a) {
        return "\t/" + a.getAbilityName() + " - " + a.getInfo() + "\n\n";
    }
}
