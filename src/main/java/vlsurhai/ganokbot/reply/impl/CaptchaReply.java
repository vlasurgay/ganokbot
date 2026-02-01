package vlsurhai.ganokbot.reply.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.MemberStatus;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import vlsurhai.ganokbot.common.i18n.MessageResolver;
import vlsurhai.ganokbot.common.model.CaptchaPayload;
import vlsurhai.ganokbot.common.model.cache.CachedCaptchaState;
import vlsurhai.ganokbot.common.model.cache.CachedChat;
import vlsurhai.ganokbot.common.model.jpa.User;
import vlsurhai.ganokbot.common.utils.Utils;
import vlsurhai.ganokbot.reply.AbstractReply;
import vlsurhai.ganokbot.service.*;

import java.awt.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static vlsurhai.ganokbot.common.Constants.*;
import static vlsurhai.ganokbot.common.settings.SettingKey.CAPTCHA_ENABLED;
import static vlsurhai.ganokbot.common.utils.Utils.getCallback3Values;
import static vlsurhai.ganokbot.common.utils.Utils.getHtmlMention;

@Slf4j
@Component
public class CaptchaReply extends AbstractReply {

    @Value("${captcha.expiration-time.minutes:60}")
    private int expirationTime;

    @Value("${captcha.max-attempts:5}")
    private int maxAttempts;

    @Autowired
    private MessageResolver resolver;

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserService userService;

    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private ChatMemberService chatMemberService;

    @Autowired
    private TelegramApiService telegramApiService;

    @Autowired
    private ScheduledTelegramApiService scheduledActionService;

    @Override
    protected void getAction(Update update, BaseAbilityBot bot) {
        try {
            if (update.hasCallbackQuery()) {
                handleRecaptchaCallback(update.getCallbackQuery(), (AbilityBot) bot);
            } else if (update.hasMessage()) {
                processCaptcha(update, (AbilityBot) bot);
            }
        } catch (TelegramApiException e) {
            log.warn("Telegram error in CaptchaHandler: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.warn("Unexpected error in CaptchaHandler: {}", e.getMessage(), e);
        }
    }

    @Override
    protected boolean getCondition(Update update) {
        if (!isCaptchaEnabled(update)) {
            return false;
        }
        if (update.hasMessage()) {
            if (!CollectionUtils.isEmpty(update.getMessage().getNewChatMembers())) {
                return true;
            }
            Long userId = update.getMessage().getFrom().getId();
            Long chatId = update.getMessage().getChatId();

            return captchaService.hasActiveCaptcha(userId, chatId);
        }
        if (Utils.hasCallback(update, RECAPTCHA_CALLBACK)) {
            Long userId = update.getCallbackQuery().getFrom().getId();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();

            return captchaService.hasActiveCaptcha(userId, chatId);
        }
        return false;
    }

    private void handleRecaptchaCallback(CallbackQuery query, AbilityBot bot) throws TelegramApiException, IOException, FontFormatException {
        String[] parts = getCallback3Values(query.getData());

        if (parts == null || parts.length != 3) {
            return;
        }
        Long userId = Long.parseLong(parts[1]);
        Long chatId = Long.parseLong(parts[2]);

        CachedCaptchaState state = captchaService.getCaptchaState(userId, chatId);
        if (state == null || !query.getFrom().getId().equals(userId) || !query.getMessage().getChatId().equals(chatId)) {
            return;
        }
        telegramApiService.disableReplyMarkupButton(query.getMessage().getMessageId(), chatId, bot);
        sendCaptcha(userId, chatId, state.expiresAt(), getRecaptchaMessage(state, query.getFrom()), bot);
    }

    private void processCaptcha(Update update, AbilityBot bot) throws TelegramApiException, IOException, FontFormatException {
        Message message = update.getMessage();

        if (message.getNewChatMembers() != null && !message.getNewChatMembers().isEmpty()) {
            handleJoin(message, bot);
        } else if (message.hasText()) {
            handleTextMessage(message, bot);
        } else {
            handleOtherMessage(message, bot);
        }
    }

    private void handleJoin(Message message, AbilityBot bot) throws TelegramApiException, IOException, FontFormatException {
        for (org.telegram.telegrambots.meta.api.objects.User apiUser : message.getNewChatMembers()) {
            if (apiUser.getIsBot()) {
                continue;
            }
            User user = userService.getOrCreateUser(apiUser.getId(), false);
            CachedChat chat = chatService.getChat(message.getChatId());

            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(expirationTime);

            sendCaptcha(user.getUserId(), chat.chatId(), expiresAt, getCaptchaMessage(apiUser), bot);
            scheduledActionService.scheduleUserBan(chat.chatId(), user.getUserId(), expiresAt);
        }
    }

    private void handleTextMessage(Message message, AbilityBot bot) throws TelegramApiException {
        Long userId = message.getFrom().getId();
        Long chatId = message.getChatId();
        CachedCaptchaState state = captchaService.getCaptchaState(userId, chatId);

        scheduledActionService.scheduleMessageDeletion(state.chatId(), state.userId(), message.getMessageId().longValue(), state.expiresAt());

        if (state.captchaText().equals(message.getText().trim())) {
            cleanupCaptcha(userId, chatId, bot);
            chatMemberService.createOrUpdateChatMember(userId, chatId, MemberStatus.MEMBER);
            log.warn("User({}) in chat({}) successfully passed captcha", userId, chatId);
            return;
        }
        String caption = resolver.getMessage(INCORRECT_CAPTCHA_EXCEPTION_MESSAGE);
        incrementAttempts(userId, chatId, message.getMessageId(), caption, bot);
    }

    private void handleOtherMessage(Message message, AbilityBot bot) throws TelegramApiException {
        Long userId = message.getFrom().getId();
        Long chatId = message.getChatId();

        String caption = resolver.getMessage(CAPTCHA_IGNORE_EXCEPTION_MESSAGE);

        telegramApiService.deleteMessage(
                message.getMessageId().longValue(), message.getChatId(), bot
        );
        incrementAttempts(
                userId, chatId, message.getMessageId(), caption, bot
        );
    }

    private void sendCaptcha(Long userId, Long chatId, LocalDateTime expiresAt, String captchaMessage, AbilityBot bot) throws IOException, FontFormatException, TelegramApiException {
        CaptchaPayload captcha = captchaService.createCaptcha(userId, chatId, expiresAt);
        ReplyKeyboard replyKeyboard = buildCallbackKeyboard(userId, chatId);
        Message response = telegramApiService.sendAnimationSync(chatId, captcha.captcha(), captchaMessage, replyKeyboard, bot);

        if (response != null) {
            scheduledActionService.scheduleMessageDeletion(chatId, userId, response.getMessageId().longValue(), expiresAt);
        }
    }

    private void cleanupCaptcha(Long targetUserId, Long chatId, AbilityBot bot) {
        scheduledActionService.cancelScheduledUserBan(chatId, targetUserId);
        scheduledActionService.runScheduledMessagesDeletion(chatId, targetUserId, bot);
        captchaService.cleanupCaptchaState(targetUserId, chatId);
    }

    private InlineKeyboardMarkup buildCallbackKeyboard(Long userId, Long chatId) {
        String callbackData = String.format(CALLBACK_FORM_3, RECAPTCHA_CALLBACK, userId, chatId);
        String caption = resolver.getMessage(RESEND_CAPTCHA_CAPTION);

        InlineKeyboardButton button = InlineKeyboardButton.builder()
                .text(caption)
                .callbackData(callbackData)
                .build();
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(button))
                .build();
    }

    private void incrementAttempts(Long userId, Long chatId, Integer messageId, String exceptionMessage, AbilityBot bot) throws TelegramApiException {
        CachedCaptchaState updatedState = captchaService.incrementAttempts(userId, chatId);

        if (updatedState.attempts() >= maxAttempts - 1) {
            cleanupCaptcha(userId, chatId, bot);
            scheduledActionService.runScheduledUserBan(chatId, userId, bot);

            log.warn("User({}) in chat({}) has not passed captcha", userId, chatId);
        } else {
            Message response = telegramApiService.sendMessageSync(
                    chatId, messageId, exceptionMessage, bot
            );
            scheduledActionService.scheduleMessageDeletion(chatId, userId, response.getMessageId().longValue(), updatedState.expiresAt());
        }
    }

    private String getRecaptchaMessage(CachedCaptchaState captchaState, org.telegram.telegrambots.meta.api.objects.User user) {
        String htmlMention = getHtmlMention(user);
        String caption = resolver.getMessage(ATTEMPTS_LEFT_MESSAGE);
        int attemptsLeft = maxAttempts - captchaState.attempts();

        return String.format(caption, htmlMention, attemptsLeft);
    }

    private String getCaptchaMessage(org.telegram.telegrambots.meta.api.objects.User user) {
        String htmlMention = getHtmlMention(user);
        String caption = resolver.getMessage(SEND_CAPTCHA_MESSAGE);

        return String.format(caption, htmlMention, expirationTime, maxAttempts);
    }

    private boolean isCaptchaEnabled(Update update) {
        if (update.hasMessage()) {
            return chatService.isSettingEnabled(update.getMessage().getChatId(), CAPTCHA_ENABLED.getKey());
        }
        else if (update.hasCallbackQuery()) {
            return chatService.isSettingEnabled(update.getCallbackQuery().getMessage().getChatId(), CAPTCHA_ENABLED.getKey());
        }
        return false;
    }
}
