package vlsurhai.ganokbot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.groupadministration.*;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;

import static vlsurhai.ganokbot.common.Constants.HTML;

@Service
public class TelegramApiService {

    private static final Logger log = LoggerFactory.getLogger(TelegramApiService.class);



    public void sendMessage(Long chatId, String text, AbilityBot bot) throws TelegramApiException {
        SendMessage message = buildSendMessage(chatId, text);
        sendAsyncMessage(chatId, message, bot);
    }

    public void sendMessageReply(Long chatId, Integer replyMessageId, String text, AbilityBot bot) throws TelegramApiException {
        SendMessage message = buildSendMessageReply(chatId, replyMessageId, text);
        sendAsyncMessage(chatId, message, bot);
    }

    public Message sendMessageSync(Long chatId, Integer replyMessageId, String text, AbilityBot bot) throws TelegramApiException {
        SendMessage message = buildSendMessageReply(chatId, replyMessageId, text);
        Message response = bot.execute(message);
        log.info("Message({}) in chat({}) has been sent: {}", response, chatId, response.getMessageId());
        return response;
    }

    public void sendMessageWithKeyboard(Long chatId, String text, ReplyKeyboard replyKeyboard, AbilityBot bot) throws TelegramApiException {
        SendMessage message = buildSendMessageWithKeyboard(chatId, text, replyKeyboard);
        sendAsyncMessage(chatId, message, bot);
    }

    public void sendMessageReplyWithKeyboard(Long chatId, String text, Integer replyMessageId, ReplyKeyboard replyKeyboard, AbilityBot bot) throws TelegramApiException {
        SendMessage message = buildSendMessageReplyWithKeyboard(chatId, text, replyMessageId, replyKeyboard);
        sendAsyncMessage(chatId, message, bot);
    }

    public Message sendAnimationSync(Long chatId, InputFile animation, String text, ReplyKeyboard replyKeyboard, AbilityBot bot) throws TelegramApiException {
        SendAnimation message = buildSendAnimation(chatId, animation, text, replyKeyboard);
        Message response = bot.execute(message);
        log.info("Animation in chat({}) has been sent: {}", chatId, text);
        return response;
    }

    private void sendAsyncMessage(Long chatId, BotApiMethod<Message> method, AbilityBot bot) throws TelegramApiException {
        bot.executeAsync(method)
                .whenComplete((response, exception) -> {

                    if (exception != null) {
                        log.error("Failed to send async message: {}", exception.getMessage(), exception);
                    } else {
                        log.info("Message in chat({}) has been sent. MessageId: {}", chatId, response.getMessageId());
                    }
        });
    }


    private SendAnimation buildSendAnimation(Long chatId, InputFile animation, String text, ReplyKeyboard replyKeyboard) {
        return SendAnimation.builder()
                .animation(animation)
                .chatId(chatId)
                .caption(text)
                .parseMode(HTML)
                .replyMarkup(replyKeyboard)
                .build();
    }

    private SendMessage buildSendMessageWithKeyboard(Long chatId, String text, ReplyKeyboard replyKeyboard) {
        return SendMessage.builder()
                .chatId(chatId.toString())
                .replyMarkup(replyKeyboard)
                .parseMode(HTML)
                .text(text)
                .build();
    }

    private SendMessage buildSendMessageReplyWithKeyboard(Long chatId, String text, Integer replyMessageId, ReplyKeyboard replyKeyboard) {
        return SendMessage.builder()
                .chatId(chatId.toString())
                .replyMarkup(replyKeyboard)
                .replyToMessageId(replyMessageId)
                .parseMode(HTML)
                .text(text)
                .build();
    }

    private SendMessage buildSendMessageReply(Long chatId, Integer replyMessageId, String text) {
        return SendMessage.builder()
                .chatId(chatId.toString())
                .replyToMessageId(replyMessageId)
                .parseMode(HTML)
                .text(text)
                .build();
    }

    private SendMessage buildSendMessage(Long chatId, String text) {
        return SendMessage.builder()
                .chatId(chatId.toString())
                .parseMode(HTML)
                .text(text)
                .build();
    }

    public Chat getFullChat(Long chatId, AbilityBot bot) throws TelegramApiException {
        GetChat getChat = buildGetChat(chatId);
        return bot.execute(getChat);
    }

    private GetChat buildGetChat(Long chatId) {
        return GetChat.builder().chatId(chatId).build();
    }


    public ArrayList<ChatMember> getChatAdministrators(Long chatId, AbilityBot bot) throws TelegramApiException {
        GetChatAdministrators getChatAdministrators = buildGetChatAdministrators(chatId);
        return bot.execute(getChatAdministrators);
    }

    private GetChatAdministrators buildGetChatAdministrators(Long chatId) {
        return GetChatAdministrators.builder()
                .chatId(chatId)
                .build();
    }

    public void disableReplyMarkupButton(Integer messageId, Long chatId, AbilityBot bot) throws TelegramApiException {
        EditMessageReplyMarkup editMessageReplyMarkup = buildEditMessageReplyMarkup(messageId, chatId, null);
        bot.execute(editMessageReplyMarkup);
        log.info("Reply markup of message({}) in chat({}) has been updated.", messageId, chatId);
    }

    private EditMessageReplyMarkup buildEditMessageReplyMarkup(Integer messageId, Long chatId, InlineKeyboardMarkup keyboardMarkup) {
        return  EditMessageReplyMarkup.builder()
                .chatId(chatId)
                .messageId(messageId)
                .replyMarkup(keyboardMarkup)
                .build();
    }

    public void deleteMessage(Long messageId, Long chatId, AbilityBot bot) throws TelegramApiException {
        DeleteMessage deleteMessage = buildDeleteMessage(messageId, chatId);
        bot.execute(deleteMessage);
        log.info("Message({}) in chat({}) has been deeleted.", messageId, chatId);
    }

    private DeleteMessage buildDeleteMessage(Long messageId, Long chatId) {
        return DeleteMessage.builder()
                .messageId(messageId.intValue())
                .chatId(chatId.toString())
                .build();
    }


    public ChatMember getChatMember(Long userId, Long chatId, AbilityBot bot) throws TelegramApiException {
        GetChatMember banChatMember = buildGetChatMember(userId, chatId);
        return bot.execute(banChatMember);
    }

    private GetChatMember buildGetChatMember(Long userId, Long chatId) {
        return GetChatMember.builder()
                .userId(userId)
                .chatId(chatId)
                .build();
    }


    public void banChatMember(Long userId, Long chatId, AbilityBot bot) throws TelegramApiException {
        BanChatMember banChatMember = buildBanChatMember(userId, chatId);
        bot.execute(banChatMember);
        log.info("User({}) in chat({}) has been banned.", userId, chatId);
    }

    private BanChatMember buildBanChatMember(Long userId, Long chatId) {
        return BanChatMember.builder()
                .userId(userId)
                .chatId(chatId)
                .revokeMessages(true)
                .build();
    }


    public void updateUserPermissions(Long targetUserId, Long chatId, Integer untilDate,
                                      org.telegram.telegrambots.meta.api.objects.ChatPermissions chatPermissions, AbilityBot bot) throws TelegramApiException {
        int duration = untilDate == null ? 0 : untilDate;
        RestrictChatMember restrictChatMember = buildRestrictChatMember(chatId, targetUserId, duration, chatPermissions);
        bot.execute(restrictChatMember);
    }

    private RestrictChatMember buildRestrictChatMember(Long chatId, Long targetUserId, Integer untilDate,
                                                       org.telegram.telegrambots.meta.api.objects.ChatPermissions chatPermissions) {
        return RestrictChatMember.builder()
                .chatId(chatId)
                .userId(targetUserId)
                .permissions(chatPermissions)
                .useIndependentChatPermissions(true)
                .untilDate(untilDate)
                .build();
    }


    public void updateChatPermissions(Long chatId, org.telegram.telegrambots.meta.api.objects.ChatPermissions tgPermissions, AbilityBot bot) throws TelegramApiException {
        SetChatPermissions setChatPermissions = buildSetChatPermissions(chatId, tgPermissions);
        bot.execute(setChatPermissions);
    }

    private SetChatPermissions buildSetChatPermissions(Long chatId, org.telegram.telegrambots.meta.api.objects.ChatPermissions tgPermissions) {
        return SetChatPermissions.builder()
                .chatId(chatId)
                .permissions(tgPermissions)
                .useIndependentChatPermissions(true)
                .build();
    }
}
