package vlsurhai.ganokbot.ability.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import vlsurhai.ganokbot.ability.AbstractAbility;
import vlsurhai.ganokbot.common.i18n.MessageResolver;
import vlsurhai.ganokbot.common.model.cache.CachedChat;
import vlsurhai.ganokbot.service.ChatPermissionsService;
import vlsurhai.ganokbot.service.ChatService;
import vlsurhai.ganokbot.service.ComplaintService;
import vlsurhai.ganokbot.service.TelegramApiService;

import java.time.LocalDateTime;

import static org.telegram.abilitybots.api.objects.Locality.GROUP;
import static vlsurhai.ganokbot.common.Constants.*;
import static vlsurhai.ganokbot.common.settings.SettingKey.COMPLAINTS_TO_MUTE;
import static vlsurhai.ganokbot.common.settings.SettingKey.COMPLAIN_ENABLED;
import static vlsurhai.ganokbot.common.utils.Utils.getHtmlMention;

@Slf4j
@Component
public class ComplainAbility extends AbstractAbility {

    @Value("${complaints.expiration-time.days:1}")
    private int complaintsExpirationTime;

    @Autowired
    private MessageResolver resolver;

    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatPermissionsService chatPermissionsService;

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private TelegramApiService telegramApiService;

    @Override
    public String getAbilityName() {
        return COMPLAIN_ABILITY;
    }

    @Override
    public String getInfo() {
        return resolver.getMessage(COMPLAIN_INFO);
    }

    @Override
    public Locality getLocality() {
        return GROUP;
    }

    @Override
    protected void getAction(MessageContext ctx) {
        Long chatId = ctx.update().getMessage().getChatId();
        CachedChat chat = chatService.getChat(chatId);
        Message message = ctx.update().getMessage();
        Message replyToMessage = message.getReplyToMessage();

        try {
            if (!isComplainingEnabled(chat)) {
                telegramApiService.sendMessageReply(
                        chatId, message.getMessageId(), resolver.getMessage(COMPLAINING_DISABLED_EXCEPTION_MESSAGE), (AbilityBot) ctx.bot()
                );
            }
            else if (replyToMessage != null) {
                processComplaint(chat, message, replyToMessage, (AbilityBot) ctx.bot());
            }
            else {
                telegramApiService.sendMessageReply(
                        chatId, message.getMessageId(), resolver.getMessage(INCORRECT_COMMAND_EXCEPTION_MESSAGE), (AbilityBot) ctx.bot()
                );
            }
        } catch (TelegramApiException e) {
            log.info("Exception while registering complaint: {}", e.getMessage(), e);
        }
    }

    private void processComplaint(CachedChat chat, Message message, Message replyToMessage, AbilityBot bot) throws TelegramApiException {
        Long chatId = chat.chatId();
        Long initiatorId = message.getFrom().getId();
        Long targetUserId = replyToMessage.getFrom().getId();

        if (complaintService.hasUserComplained(initiatorId, targetUserId, chatId)) {
            String caption = resolver.getMessage(ALREADY_COMPLAINED_EXCEPTION_MESSAGE);

            telegramApiService.sendMessageReply(
                    message.getChatId(), message.getMessageId(), caption, bot
            );
        }
        else if (isEnoughToMute(chat, targetUserId)) {
            processMuteByComplaints(message, replyToMessage.getFrom(), bot);
            log.info("User({}) in chat({}) has been muted due to the accumulated number of complaints", targetUserId, chatId);
        }
        else {
            processRegisterComplaint(message, replyToMessage.getFrom(), bot);
            log.info("Complaint from user({}) at user({}) in chat({}) has been registered", initiatorId, targetUserId, chatId);
        }
    }

    private void processMuteByComplaints(Message message, org.telegram.telegrambots.meta.api.objects.User targetUser, AbilityBot bot) throws TelegramApiException {
        String caption = String.format(resolver.getMessage(MUTE_MESSAGE), getHtmlMention(targetUser));

        telegramApiService.updateUserPermissions(
                targetUser.getId(), message.getChatId(), null, chatPermissionsService.mutedPermissions(), bot
        );

        complaintService.cleanupComplaints(targetUser.getId(), message.getChatId());

        telegramApiService.sendMessageReply(message.getChatId(), message.getMessageId(), caption, bot);
    }

    private void processRegisterComplaint(Message message, org.telegram.telegrambots.meta.api.objects.User targetUser, AbilityBot bot) throws TelegramApiException {
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(complaintsExpirationTime);
        String caption = String.format(resolver.getMessage(COMPLAINT_MESSAGE), getHtmlMention(targetUser));

        complaintService.registerComplaint(message.getChatId(), message.getFrom().getId(), targetUser.getId(), expiresAt);

        telegramApiService.sendMessageReply(message.getChatId(), message.getMessageId(), caption, bot);
    }

    private boolean isEnoughToMute(CachedChat chat, Long targetUserId) {
        return complaintService.getComplaintsAmount(chat.chatId(), targetUserId) + 1 >= getComplaintsToMute(chat);
    }

    private boolean isComplainingEnabled(CachedChat chat) {
        String isComplainEnabled = chat.settings().get(COMPLAIN_ENABLED.getKey());
        return Boolean.parseBoolean(isComplainEnabled);
    }

    private int getComplaintsToMute(CachedChat chat) {
        String complaintsToMute = chat.settings().getOrDefault(COMPLAINTS_TO_MUTE.getKey(), Integer.toString(5));
        return Integer.parseInt(complaintsToMute);
    }
}
