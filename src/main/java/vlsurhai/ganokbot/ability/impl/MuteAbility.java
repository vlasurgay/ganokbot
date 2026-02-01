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
import vlsurhai.ganokbot.service.ChatPermissionsService;
import vlsurhai.ganokbot.service.TelegramApiService;

import java.time.Duration;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static vlsurhai.ganokbot.common.Constants.*;
import static vlsurhai.ganokbot.common.utils.Utils.getHtmlMention;
import static vlsurhai.ganokbot.common.utils.Utils.parseDuration;

@Slf4j
@Component
public class MuteAbility extends AbstractAbility {

    private static final Pattern MUTE_COMMAND_PATTERN = Pattern.compile(SLASH + MUTE_ABILITY + DURATION_REGEXP, Pattern.CASE_INSENSITIVE);

    @Autowired
    private MessageResolver resolver;

    @Autowired
    private TelegramApiService telegramApiService;

    @Autowired
    private ChatPermissionsService chatPermissionsService;

    @Override
    public String getAbilityName() {
        return MUTE_ABILITY;
    }

    @Override
    public String getInfo() { return resolver.getMessage(MUTE_INFO); }

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
        Message replyMessage = message.getReplyToMessage();
        Matcher matcher = MUTE_COMMAND_PATTERN.matcher(message.getText());

        try {
            if (replyMessage == null || !matcher.matches()) {
                String caption = resolver.getMessage(INCORRECT_COMMAND_EXCEPTION_MESSAGE);
                telegramApiService.sendMessageReply(
                        message.getChatId(), message.getMessageId(), caption, (AbilityBot) ctx.bot()
                );
            } else {
                Duration duration = parseDuration(matcher.group(1));
                processMuteUser(message, duration, (AbilityBot) ctx.bot());
                log.info("User({}) in chat({}) has been muted.", replyMessage.getFrom().getId(), ctx.chatId());
            }
        } catch (TelegramApiException e) {
            log.warn("Failed to mute user({}) in chat({}): {}", replyMessage.getFrom().getId(), message.getChatId(), e.getMessage(), e);
        }
    }

    private void processMuteUser(Message message, Duration duration, AbilityBot bot) throws TelegramApiException {
        User targetUser = message.getReplyToMessage().getFrom();
        String htmlMention = getHtmlMention(targetUser);
        int untilDate = (int) Instant.now().plus(duration).getEpochSecond();

        telegramApiService.updateUserPermissions(
                targetUser.getId(), message.getChatId(), untilDate, chatPermissionsService.mutedPermissions(), bot
        );

        String caption = String.format(resolver.getMessage(MUTE_MESSAGE), htmlMention);
        telegramApiService.sendMessageReply(message.getChatId(), message.getMessageId(), caption, bot);
    }
}
