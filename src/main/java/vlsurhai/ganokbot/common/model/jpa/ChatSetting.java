package vlsurhai.ganokbot.common.model.jpa;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@Table(name = "chat_settings")
public class ChatSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_id", nullable = false)
    private Long chatId;

    @Column(nullable = false)
    private String key;

    @Column()
    private String value;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public ChatSetting(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @PrePersist
    public void prePersist() {
        this.updatedAt = LocalDateTime.now();
    }
}
