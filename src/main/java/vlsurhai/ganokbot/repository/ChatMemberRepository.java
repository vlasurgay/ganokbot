package vlsurhai.ganokbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vlsurhai.ganokbot.common.model.jpa.Chat;
import vlsurhai.ganokbot.common.model.jpa.ChatMember;

import java.util.List;
import java.util.Optional;

public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {

    Optional<ChatMember> findByUserIdAndChatId(Long userId, Long chatId);
    void deleteByUserIdAndChatId(Long userId, Long chatId);
    void deleteByChatId(Long chatId);

    @Query("select distinct c from Chat c " +
            "join ChatMember cm on c.chatId = cm.chatId " +
            "where cm.userId = :userId and cm.status in :statuses")
    List<Chat> findChatsByUserStatus( @Param("userId") Long userId,
                                      @Param("statuses") String ... statuses);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update ChatMember cm set cm.status = :newStatus " +
            "where cm.chatId = :chatId and cm.status in :statuses and cm.userId not in :keepUserIds")
    int updateChatMemberStatusesNotIn(@Param("chatId") Long chatId, @Param("keepUserIds") List<Long> keepUserIds,
                             @Param("newStatus") String newStatus, @Param("statuses") List<String> statuses);
}
