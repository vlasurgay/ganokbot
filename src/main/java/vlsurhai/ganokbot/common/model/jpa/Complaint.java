package vlsurhai.ganokbot.common.model.jpa;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@Table(name = "complaints")
public class Complaint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "chat_id", nullable = false)
    private Long chatId;

    @JoinColumn(name = "target_user_id", nullable = false)
    private Long targetUserId;

    @JoinColumn(name = "initiator_id", nullable = false)
    private Long initiatorId;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    public Complaint(Long chatId, Long targetUserId, Long initiatorId, LocalDateTime expiresAt) {
        this.chatId = chatId;
        this.targetUserId = targetUserId;
        this.initiatorId = initiatorId;
        this.expiresAt = expiresAt;
    }
}