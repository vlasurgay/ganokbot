package vlsurhai.ganokbot.ability.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.MemberStatus;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import vlsurhai.ganokbot.ability.AbstractAbility;
import vlsurhai.ganokbot.common.i18n.MessageResolver;
import vlsurhai.ganokbot.common.model.SettingsState;
import vlsurhai.ganokbot.common.model.jpa.Chat;
import vlsurhai.ganokbot.common.model.jpa.ChatSetting;
import vlsurhai.ganokbot.common.settings.SettingInputParser;
import vlsurhai.ganokbot.common.settings.SettingKey;
import vlsurhai.ganokbot.common.utils.Utils;
import vlsurhai.ganokbot.service.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static vlsurhai.ganokbot.common.Constants.*;
import static vlsurhai.ganokbot.common.settings.SettingType.BOOLEAN;
import static vlsurhai.ganokbot.common.utils.Utils.getCallback2Value;

@Slf4j
@Component
public class SetUpAbility extends AbstractAbility {

    @Autowired
    private ChatService chatService;

    @Autowired
    private MessageResolver resolver;

    @Autowired
    private ChatMemberService chatMemberService;

    @Autowired
    private ChatSettingService chatSettingService;

    @Autowired
    private UserService userService;

    @Autowired
    private TelegramApiService telegramApiService;

    @Autowired
    private SettingInputParser settingInputParser;

    @Override
    public String getAbilityName() {
        return SETUP_ABILITY;
    }

    @Override
    public Locality getLocality() {
        return Locality.USER;
    }

    @Override
    public String getInfo() {
        return resolver.getMessage(SETUP_INFO);
    }

    @Override
    public Ability buildAbility() {
        return Ability.builder()
                .name(getAbilityName())
                .info(getInfo())
                .locality(getLocality())
                .privacy(getPrivacy())
                .action(this::getAction)
                .reply(this::handleResetup, this::isSetupCommand, chatSettingService::isBeingSetUp)
                .reply(this::handleGroup, this::hasSetupCallback, chatSettingService::isBeingSetUp)
                .reply(this::handleInput, this::hasInputMessage, chatSettingService::isBeingSetUp)
                .build();
    }

    @Override
    protected void getAction(MessageContext ctx) {
        Long userId = ctx.user().getId();
        userService.getOrCreateUser(userId, ctx.user().getIsBot());
        try {
            List<org.telegram.telegrambots.meta.api.objects.Chat> adminGroups = getAdminGroupsForUser(ctx.user().getId(), (AbilityBot) ctx.bot());
            provideGroupSelection(userId, adminGroups, ctx);

        } catch (TelegramApiException e) {
            log.info("Exception in SetUpAbility.execute in chat({}): {}", ctx.chatId(), e.getMessage(), e);
        }
    }

    private void provideGroupSelection(Long userId, List<org.telegram.telegrambots.meta.api.objects.Chat> adminGroups, MessageContext ctx) throws TelegramApiException {
        if (adminGroups.isEmpty()) {
            telegramApiService.sendMessageReply(
                    ctx.chatId(), ctx.update().getMessage().getMessageId(), resolver.getMessage(NO_ADMIN_GROUPS_EXCEPTION_MESSAGE), (AbilityBot) ctx.bot()
            );
        } else {
            chatSettingService.createState(userId, ctx.chatId());
            telegramApiService.sendMessageWithKeyboard(
                    ctx.chatId(), resolver.getMessage(CHOOSE_GROUP_CAPTION), buildGroupSelectionKeyboard(adminGroups), (AbilityBot) ctx.bot()
            );
        }
    }

    private void handleResetup(BaseAbilityBot bot, Update update) {
        SettingsState state = chatSettingService.getState(update);
        if (state == null) {
            return;
        }
        try {
            telegramApiService.sendMessage(
                    state.getCurrentChatId(), resolver.getMessage(RESETUP_MESSAGE), (AbilityBot) bot
            );
        } catch (TelegramApiException e) {
            log.info("Exception in SetUpAbility.handleResetup in chat({}): {}", state.getCurrentChatId(), e.getMessage(), e);
        }
        askNext(state, bot);
    }

        private void handleGroup(BaseAbilityBot bot, Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long currentChatId = callbackQuery.getMessage().getChatId();

        SettingsState state = chatSettingService.getState(update);
        if (state == null) {
            return;
        }
        String targetChatId = getCallback2Value(callbackQuery.getData());
        if (targetChatId == null) {
            return;
        }
        state.setTargetChatId(Long.valueOf(targetChatId));
        state.setUpdatedAt(LocalDateTime.now());

        try {
            org.telegram.telegrambots.meta.api.objects.Chat targetChat = telegramApiService.getFullChat(Long.valueOf(targetChatId), (AbilityBot) bot);
            telegramApiService.sendMessage(
                    currentChatId, String.format(resolver.getMessage(CHOSEN_CHAT_CAPTION), targetChat.getTitle()), (AbilityBot) bot
            );
        } catch (TelegramApiException e) {
            log.info("Exception in SetUpAbility.handleGroup in chat({}): {}", currentChatId, e.getMessage(), e);
        }
        askNext(state, bot);
    }

    private void handleInput(BaseAbilityBot bot, Update update) {
        SettingsState state = chatSettingService.getState(update);
        if (state == null) {
            return;
        }
        String input = update.getMessage().getText().trim();
        SettingKey currentStep = state.currentStep();

        if (resolver.getMessage(SKIP_CAPTION).equals(input)) {
            state.nextStep();
            askNext(state, bot);
            return;
        }
        ChatSetting chatSetting = buildChatSetting(currentStep, input);
        if (chatSetting != null) {
            state.addSetting(currentStep, chatSetting);
            log.info("Added a setting({}) for chat({})", chatSetting.getKey(), chatSetting.getValue());

            state.nextStep();
            askNext(state, bot);
            return;
        }
        try {
            Integer messageId = update.hasMessage() ? update.getMessage().getMessageId() : update.getCallbackQuery().getMessage().getMessageId();
            telegramApiService.sendMessageReplyWithKeyboard(
                    state.getCurrentChatId(), resolver.getMessage(INVALID_INPUT_EXCEPTION_MESSAGE), messageId, buildKeyboard(currentStep), (AbilityBot) bot
            );
        } catch (TelegramApiException e) {
            log.info("Exception in SetUpAbility.handleInput in chat({}): {}", state.getCurrentChatId(), e.getMessage(), e);
        }
    }

    private ChatSetting buildChatSetting(SettingKey currentKey, String input) {
        if (resolver.getMessage(DEFAULT_CAPTION).equals(input)) {
            return new ChatSetting(currentKey.getKey(), currentKey.getDefaultValue());
        }
        String settingValue = settingInputParser.parse(input, currentKey);
        return settingValue != null ? new ChatSetting(currentKey.getKey(), settingValue) : null;
    }

    private void askNext(SettingsState state, BaseAbilityBot bot) {
        state.skipStepIfNeeded();
        try {
            SettingKey currentStep = state.currentStep();
            if (currentStep == null) {
                finalizeSettings(bot, state);
                return;
            }
            telegramApiService.sendMessageWithKeyboard(
                    state.getCurrentChatId(), resolver.getMessage(currentStep.getCaption()), buildKeyboard(currentStep), (AbilityBot) bot
            );
        } catch (TelegramApiException e) {
            log.info("Exception in SetUpAbility.askNext in chat({}): {}", state.getCurrentChatId(), e.getMessage(), e);
        }
    }

    private void finalizeSettings(BaseAbilityBot bot, SettingsState state) throws TelegramApiException {
        Long chatId = state.getTargetChatId();

        chatService.updateChatSettings(chatId, state.getChatSettings());
        chatSettingService.deleteState(state.getUserId());

        telegramApiService.sendMessage(
                state.getCurrentChatId(), resolver.getMessage(SETUP_COMPLETED_CAPTION), (AbilityBot) bot
        );
    }

    private List<org.telegram.telegrambots.meta.api.objects.Chat> getAdminGroupsForUser(Long userId, AbilityBot bot) throws TelegramApiException {
        List<Chat> chats = chatMemberService.findChatsByUserStatus(userId, MemberStatus.ADMINISTRATOR, MemberStatus.CREATOR);
        List<org.telegram.telegrambots.meta.api.objects.Chat> adminGroups = new ArrayList<>();

        for (Chat chat : chats) {
            if (isAdminInChat(userId, chat.getChatId(), bot)) {
                adminGroups.add(telegramApiService.getFullChat(chat.getChatId(), bot));
            }
        }
        return adminGroups;
    }

    private boolean isAdminInChat(Long userId, Long chatId, AbilityBot bot) throws TelegramApiException {
        return telegramApiService.getChatAdministrators(chatId, bot).stream()
                .anyMatch(
                        chatMember -> chatMember.getUser().getId().equals(userId)
                );
    }


    private ReplyKeyboardMarkup buildKeyboard(SettingKey key) {
        List<KeyboardRow> rows = new ArrayList<>();

        KeyboardRow payloadRow = new KeyboardRow();
        if (BOOLEAN.equals(key.getType())) {
            payloadRow.addAll(List.of(resolver.getMessage(YES), resolver.getMessage(NO)));
        } else if (key.hasDefaultValue()) {
            payloadRow.addAll(List.of(resolver.getMessage(DEFAULT_CAPTION)));
        }
        rows.add(payloadRow);

        if (key.isSkippable()) {
            KeyboardRow skipRow = new KeyboardRow();
            skipRow.add(resolver.getMessage(SKIP_CAPTION));
            rows.add(skipRow);
        }

        return ReplyKeyboardMarkup.builder()
                .keyboard(rows)
                .resizeKeyboard(true)
                .oneTimeKeyboard(true)
                .build();
    }


    private InlineKeyboardMarkup buildGroupSelectionKeyboard(List<org.telegram.telegrambots.meta.api.objects.Chat> groups) {
        List<List<InlineKeyboardButton>> rows = groups.stream()
                .map(group -> List.of(
                        InlineKeyboardButton.builder()
                                .text(group.getTitle())
                                .callbackData(String.format(CALLBACK_FORM_2, SETUP_GROUP_CALLBACK, group.getId()))
                                .build()
                ))
                .toList();

        return new InlineKeyboardMarkup(rows);
    }

    private boolean hasSetupCallback(Update update) {
        return Utils.hasCallback(update, SETUP_GROUP_CALLBACK);
    }

    private boolean isSetupCommand(Update update) {
        return update.hasMessage() && update.getMessage().isCommand()
                && update.getMessage().getText().trim().equalsIgnoreCase(SLASH + SETUP_ABILITY);
    }

    private boolean hasInputMessage(Update update) {
        return update.hasMessage() && update.getMessage().hasText()
                && !update.getMessage().getText().trim().isEmpty()
                && !update.getMessage().isCommand();
    }
}
