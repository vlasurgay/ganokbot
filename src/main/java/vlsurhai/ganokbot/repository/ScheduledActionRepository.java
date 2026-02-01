package vlsurhai.ganokbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vlsurhai.ganokbot.common.model.jpa.ScheduledAction;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduledActionRepository extends JpaRepository<ScheduledAction, Long> {

    List<ScheduledAction> findByTypeAndChatIdAndUserId(ScheduledAction.ActionType type, long chatId, long userId);
    int deleteByTypeAndChatIdAndUserId(ScheduledAction.ActionType type, long chatId, long userId);

    @Query("select a from ScheduledAction a where a.executeAt <= :now")
    List<ScheduledAction> findExpiredScheduledActions(@Param("now") LocalDateTime now);

    @Query("select min(a.executeAt) from ScheduledAction a")
    LocalDateTime findNextExecuteAt();
}