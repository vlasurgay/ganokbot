package vlsurhai.ganokbot.common.model.jpa;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @Column(name = "user_id")
    private Long userId;

    private Boolean isBot = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public User(Long userId) {
        this.userId = userId;
    }

    public User(Long userId, Boolean isBot) {
        this.userId = userId;
        this.isBot = isBot;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}