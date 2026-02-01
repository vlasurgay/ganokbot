package vlsurhai.ganokbot.common.model.jpa;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "chats")
public class Chat {

    @Id
    @Column(name = "chat_id")
    private Long chatId;

    private String type = "group";

    @OneToMany(mappedBy = "chatId", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ChatSetting> settings = new ArrayList<>();

    public Chat(Long chatId) {
        this.chatId = chatId;
    }

    public Chat(Long chatId, String type) {
        this.chatId = chatId;
        this.type = type;
    }
}