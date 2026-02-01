package vlsurhai.ganokbot.common.scheduling;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import vlsurhai.ganokbot.common.model.jpa.ScheduledAction;
import vlsurhai.ganokbot.service.TelegramApiService;

@Component
public class ScheduledActionExecutor {

    @Autowired
    private TelegramApiService telegramApiService;

    public void execute(ScheduledAction action, AbilityBot bot) throws TelegramApiException {
        switch (action.getType()) {
            case BAN_USER ->
                    telegramApiService.banChatMember(action.getUserId(), action.getChatId(), bot);
            case DELETE_MESSAGE ->
                    telegramApiService.deleteMessage(action.getMessageId(), action.getChatId(), bot);
        }
    }
}