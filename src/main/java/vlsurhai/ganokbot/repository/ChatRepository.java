package vlsurhai.ganokbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vlsurhai.ganokbot.common.model.jpa.Chat;

public interface ChatRepository extends JpaRepository<Chat, Long> {
}
