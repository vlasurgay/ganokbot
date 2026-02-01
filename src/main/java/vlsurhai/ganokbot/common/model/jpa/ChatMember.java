package vlsurhai.ganokbot.common.model.jpa;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.MemberStatus;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@Table(name = "chat_members")
public class ChatMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "user_id", nullable = false)
    private Long userId;

    @JoinColumn(name = "chat_id", nullable = false, unique = true)
    private Long chatId;

    private String status = MemberStatus.MEMBER;

    private LocalDateTime joinedAt = LocalDateTime.now();

    public ChatMember(Long userId, Long chatId) {
        this.userId = userId;
        this.chatId = chatId;
    }

    public ChatMember(Long userId, Long chatId, String status) {
        this.userId = userId;
        this.chatId = chatId;
        this.status = status;
    }
}
