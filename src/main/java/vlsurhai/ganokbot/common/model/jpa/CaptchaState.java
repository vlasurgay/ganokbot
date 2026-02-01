package vlsurhai.ganokbot.common.model.jpa;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "captcha_states")
public class CaptchaState {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "user_id", nullable = false)
    private Long userId;

    @JoinColumn(name = "chat_id", nullable = false)
    private Long chatId;

    @Column(name = "captcha_text", nullable = false)
    private String captchaText;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    private Integer attempts = 0;

    public CaptchaState(Long userId, Long chatId, LocalDateTime expiresAt, String captchaText, Integer attempts) {
        this.userId = userId;
        this.chatId = chatId;
        this.expiresAt = expiresAt;
        this.captchaText = captchaText;
        this.attempts = attempts;
    }
}
