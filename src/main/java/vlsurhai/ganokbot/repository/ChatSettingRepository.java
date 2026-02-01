package vlsurhai.ganokbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vlsurhai.ganokbot.common.model.jpa.ChatSetting;

public interface ChatSettingRepository extends JpaRepository<ChatSetting, Long> {

//    @Modifying(clearAutomatically = true, flushAutomatically = true)
//    @Query("delete from ChatSetting cs where cs.chatId = :chatId and cs.key not in :keys")
//    void deleteByChatId(@Param("chatId") Long chatId, Collection<String> keys);
}
