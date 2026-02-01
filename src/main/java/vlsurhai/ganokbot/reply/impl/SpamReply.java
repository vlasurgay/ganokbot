package vlsurhai.ganokbot.reply.impl;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import vlsurhai.ganokbot.common.i18n.MessageResolver;
import vlsurhai.ganokbot.common.model.cache.CachedChat;
import vlsurhai.ganokbot.common.utils.Utils;
import vlsurhai.ganokbot.reply.AbstractReply;
import vlsurhai.ganokbot.service.ChatPermissionsService;
import vlsurhai.ganokbot.service.ChatService;
import vlsurhai.ganokbot.service.TelegramApiService;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentLinkedDeque;

import static vlsurhai.ganokbot.common.Constants.SPAM_MUTE_MESSAGE;
import static vlsurhai.ganokbot.common.settings.SettingKey.SPAM_FILTER_ENABLED;
import static vlsurhai.ganokbot.common.settings.SettingKey.SPAM_MUTE_DURATION;
import static vlsurhai.ganokbot.common.utils.Utils.encode;

@Slf4j
@Component
public class SpamReply extends AbstractReply {

    @Autowired
    private MessageResolver resolver;

    @Autowired
    private ChatService chatService;

    @Autowired
    private TelegramApiService telegramApiService;

    @Autowired
    private ChatPermissionsService chatPermissionsService;

    @Autowired
    private Cache<Long, ConcurrentLinkedDeque<Instant>> userMessageTimestamps;

    @Value("${spam.max-messages:10}")
    private int spamMaxMessages;

    @Override
    protected void getAction(Update update, BaseAbilityBot bot) {
        Long userId = update.getMessage().getFrom().getId();
        Long chatId = update.getMessage().getChatId();

        Long hash = encode(userId, chatId);
        ConcurrentLinkedDeque<Instant> timestamps = userMessageTimestamps.get(hash, k -> new ConcurrentLinkedDeque<>());

        Instant now = Instant.ofEpochSecond(update.getMessage().getDate());
        timestamps.add(now);

        if (timestamps.size() > spamMaxMessages) {
            try {
                muteUserPermissions(userId, chatId, update, (AbilityBot) bot);
                timestamps.clear();

                log.info("User {} has been muted for spam in chat {}", userId, chatId);
            } catch (TelegramApiException e) {
                log.error("Failed to mute user {} for spam in chat {}", userId, chatId, e);
            }
        }
    }

    @Override
    protected boolean getCondition(Update update) {
        return update.hasMessage() &&
                chatService.isSettingEnabled(update.getMessage().getChatId(), SPAM_FILTER_ENABLED.getKey());
    }

    private void muteUserPermissions(Long userId, Long chatId, Update update, AbilityBot bot) throws TelegramApiException {
        Duration spamMuteDuration = getMuteDuration(chatId);
        int untilDate = (int) Instant.now().plus(spamMuteDuration).getEpochSecond();

        telegramApiService.updateUserPermissions(
                userId, chatId, untilDate, chatPermissionsService.mutedPermissions(), bot
        );

        Integer messageId = update.getMessage().getMessageId();
        String caption = resolver.getMessage(SPAM_MUTE_MESSAGE);

        telegramApiService.sendMessageReply(
                chatId, messageId, String.format(caption, spamMuteDuration.toMinutes()), bot
        );
    }

    private Duration getMuteDuration(Long chatId) {
        CachedChat chat = chatService.getChat(chatId);
        String seconds = chat.settings().get(SPAM_MUTE_DURATION.getKey());
        return Utils.parseDuration(seconds);
    }
}
