package vlsurhai.ganokbot.common.scheduling;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import vlsurhai.ganokbot.common.model.jpa.ScheduledAction;
import vlsurhai.ganokbot.repository.ScheduledActionRepository;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class ScheduledActionPoller {

    @Autowired
    private ScheduledActionRepository scheduledActionRepository;

    @Autowired
    private ScheduledActionExecutor scheduledActionExecutor;

    @Autowired
    private TaskScheduler taskScheduler;

    @Autowired
    private AbilityBot bot;

    private static final long MAX_DELAY_SEC = 60;

    @PostConstruct
    public void start() {
        scheduleNextPoll();
    }

    private void scheduleNextPoll() {
        long delaySec = calculateNextDelay();
        taskScheduler.schedule(this::pollAndExecute, Instant.now().plusSeconds(delaySec));
        log.debug("Next scheduled poll in {} seconds", delaySec);
    }

    private long calculateNextDelay() {
        LocalDateTime nextAt = scheduledActionRepository.findNextExecuteAt();
        if (nextAt == null) {
            return MAX_DELAY_SEC;
        }
        long secondsToNext = Duration.between(LocalDateTime.now(), nextAt).toSeconds();
        return Math.max(1, secondsToNext);
    }

    private void pollAndExecute() {
        LocalDateTime now = LocalDateTime.now();
        List< ScheduledAction> completed = new ArrayList<>();
        List<ScheduledAction> expiredScheduledActions = scheduledActionRepository.findExpiredScheduledActions(now);

        if (!expiredScheduledActions.isEmpty()) {
            expiredScheduledActions.forEach(action -> {
                try {
                    scheduledActionExecutor.execute(action, bot);
                    completed.add(action);
                } catch (TelegramApiException e) {
                    log.debug("Error while executing scheduled action: {}", e.getMessage(), e);
                }
            });
            scheduledActionRepository.deleteAll(completed);
        }

        scheduleNextPoll();
    }
}
