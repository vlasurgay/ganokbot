package vlsurhai.ganokbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vlsurhai.ganokbot.common.model.jpa.CaptchaState;

import java.time.LocalDateTime;
import java.util.List;

public interface CaptchaStateRepository extends JpaRepository<CaptchaState, Long> {
    CaptchaState findByUserIdAndChatId(Long userId, Long chatId);
    void deleteByUserIdAndChatId(Long userId, Long chatId);
}

