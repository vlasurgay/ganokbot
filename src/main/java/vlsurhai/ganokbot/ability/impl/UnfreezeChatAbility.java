package vlsurhai.ganokbot.ability.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import vlsurhai.ganokbot.ability.AbstractAbility;
import vlsurhai.ganokbot.common.i18n.MessageResolver;
import vlsurhai.ganokbot.service.ChatPermissionsService;
import vlsurhai.ganokbot.service.TelegramApiService;

import static vlsurhai.ganokbot.common.Constants.*;

@Slf4j
@Component
public class UnfreezeChatAbility extends AbstractAbility {

    @Autowired
    private MessageResolver resolver;

    @Autowired
    private ChatPermissionsService chatPermissionsService;

    @Autowired
    private TelegramApiService telegramApiService;


    @Override
    public String getAbilityName() {
        return UNFREEZE_CHAT_ABILITY;
    }

    @Override
    public String getInfo() {
        return resolver.getMessage(UNFREEZE_CHAT_INFO);
    }

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
        if (chatPermissionsService.isFrozen(ctx.chatId())) {
            try {
                processUnfreeze(ctx.chatId(), ctx);
                log.info("Chat {} has been unfrozen", ctx.chatId());
            } catch (Exception e) {
                log.error("Failed to unfreeze chat {}: {}", ctx.chatId(), e.getMessage(), e);
            }
        }
    }

    private void processUnfreeze(Long chatId, MessageContext ctx) throws TelegramApiException {
        org.telegram.telegrambots.meta.api.objects.ChatPermissions tgPermissions =
                chatPermissionsService.unfreezeChatPermissions(chatId);

        telegramApiService.updateChatPermissions(chatId, tgPermissions, (AbilityBot) ctx.bot());

        Integer messageId = ctx.update().getMessage().getMessageId();
        String caption = resolver.getMessage(UNFREEZE_CHAT_MESSAGE);

        telegramApiService.sendMessageReply(chatId, messageId, caption, (AbilityBot) ctx.bot());
    }
}
