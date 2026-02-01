package vlsurhai.ganokbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vlsurhai.ganokbot.common.model.jpa.ChatPermissions;

public interface ChatPermissionsRepository extends JpaRepository<ChatPermissions, Long> {

    @Query("select cp.isFrozen from ChatPermissions cp where cp.chatId = :chatId")
    boolean isFrozenByChatId(@Param("chatId") Long chatId);
    ChatPermissions findByChatId(Long chatId);

}
