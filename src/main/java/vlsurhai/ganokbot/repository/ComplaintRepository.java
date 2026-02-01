package vlsurhai.ganokbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vlsurhai.ganokbot.common.model.jpa.Complaint;

import java.time.LocalDateTime;
import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    boolean existsByInitiatorIdAndTargetUserIdAndChatIdAndExpiresAtAfter(Long initiatorId, Long targetUserId, Long chatId, LocalDateTime now);
    long countByChatIdAndTargetUserIdAndExpiresAtAfter(Long chatId, Long targetUserId, LocalDateTime now);
    void deleteByTargetUserIdAndChatId(Long targetUserId, Long chatId);

    @Modifying
    @Query("delete from Complaint c where c.expiresAt <= :now")
    int deleteExpired(@Param("now") LocalDateTime now);
}
