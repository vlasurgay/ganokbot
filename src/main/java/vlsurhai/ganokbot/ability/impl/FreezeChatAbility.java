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
public class FreezeChatAbility extends AbstractAbility {

    @Autowired
    private MessageResolver resolver;

    @Autowired
    private ChatPermissionsService chatPermissionsService;

    @Autowired
    private TelegramApiService telegramApiService;

    @Override
    public String getAbilityName() {
        return FREEZE_CHAT_ABILITY;
    }

    @Override
    public String getInfo() {
        return resolver.getMessage(FREEZE_CHAT_INFO);
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
        if (!chatPermissionsService.isFrozen(ctx.chatId())) {
            try {
                processFreeze(ctx.chatId(), ctx);
                log.info("Chat {} has been frozen", ctx.chatId());
            } catch (TelegramApiException e) {
                log.info("Failed to freeze chat(id={}): {}", ctx.chatId(), e.getMessage(), e);
            }
        }
    }

    private void processFreeze(Long chatId, MessageContext ctx) throws TelegramApiException {
        AbilityBot bot = (AbilityBot) ctx.bot();
        org.telegram.telegrambots.meta.api.objects.Chat tgChat = telegramApiService.getFullChat(chatId, bot);

        chatPermissionsService.freezeChatPermissions(chatId, tgChat.getPermissions());
        telegramApiService.updateChatPermissions(chatId, chatPermissionsService.mutedPermissions(), bot);

        Integer messageId = ctx.update().getMessage().getMessageId();
        String caption = resolver.getMessage(FREEZE_CHAT_MESSAGE);

        telegramApiService.sendMessageReply(chatId, messageId, caption, (AbilityBot) ctx.bot());
    }
}
