package vlsurhai.ganokbot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import vlsurhai.ganokbot.common.scheduling.ScheduledActionExecutor;
import vlsurhai.ganokbot.common.model.jpa.ScheduledAction;
import vlsurhai.ganokbot.repository.ScheduledActionRepository;

import java.time.LocalDateTime;
import java.util.List;

import static vlsurhai.ganokbot.common.model.jpa.ScheduledAction.ActionType.BAN_USER;
import static vlsurhai.ganokbot.common.model.jpa.ScheduledAction.ActionType.DELETE_MESSAGE;

@Slf4j
@Service
public class ScheduledTelegramApiService {

    @Autowired
    private ScheduledActionExecutor scheduledActionExecutor;

    @Autowired
    private  ScheduledActionRepository repo;

    @Transactional
    public void scheduleUserBan(long chatId, long userId, LocalDateTime at) {
        scheduleAction(BAN_USER, chatId, userId, null, at);
    }

    @Transactional
    public void scheduleMessageDeletion(long chatId, long userId, long messageId, LocalDateTime at) {
        scheduleAction(DELETE_MESSAGE, chatId, userId, messageId, at);
    }

    private void scheduleAction(ScheduledAction.ActionType type, long chatId, long userId, Long messageId, LocalDateTime at) {
        if (BAN_USER.equals(type)) {
            repo.deleteByTypeAndChatIdAndUserId(type, chatId, userId);
        }
        ScheduledAction action = new ScheduledAction(type, userId, chatId, messageId, at);
        repo.save(action);
    }

    @Transactional
    public void cancelScheduledUserBan(long chatId, long userId) {
        cancelScheduledAction(BAN_USER, chatId, userId);
    }

    @Transactional
    public void cancelScheduledMessagesDeletion(long chatId, long userId) {
        cancelScheduledAction(DELETE_MESSAGE, chatId, userId);
    }

    private void cancelScheduledAction(ScheduledAction.ActionType type, long chatId, long userId) {
        repo.deleteByTypeAndChatIdAndUserId(type, chatId, userId);
    }

    @Transactional
    public void runScheduledUserBan(long chatId, long userId, AbilityBot bot) {
        runScheduledAction(BAN_USER, chatId, userId, bot);
    }

    @Transactional
    public void runScheduledMessagesDeletion(long chatId, long userId, AbilityBot bot) {
        runScheduledAction(DELETE_MESSAGE, chatId, userId, bot);
    }

    private void runScheduledAction(ScheduledAction.ActionType type, long chatId, long userId, AbilityBot bot) {
        List<ScheduledAction> scheduledActionList = repo.findByTypeAndChatIdAndUserId(type, chatId, userId);
        if (scheduledActionList == null || scheduledActionList.isEmpty()) {
            return;
        }
        scheduledActionList.forEach(action -> {
            try {
                scheduledActionExecutor.execute(action, bot);
            } catch (TelegramApiException e) {
                log.debug("Error while executing scheduled action: {}", e.getMessage(), e);
            }
        });
        repo.deleteAll(scheduledActionList);
    }
}