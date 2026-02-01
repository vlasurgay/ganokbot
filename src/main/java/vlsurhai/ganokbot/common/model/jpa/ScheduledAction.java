package vlsurhai.ganokbot.common.model.jpa;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "scheduled_actions")
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledAction {

    public ScheduledAction(ActionType type, Long userId, Long chatId, Long messageId, LocalDateTime executeAt) {
        this.type = type;
        this.userId = userId;
        this.chatId = chatId;
        this.messageId = messageId;
        this.executeAt = executeAt;
    }

    public enum ActionType {
        DELETE_MESSAGE, BAN_USER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ActionType type;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "chat_id", nullable = false)
    private Long chatId;

    @Column(name = "message_id")
    private Long messageId;

    private LocalDateTime executeAt;
}
