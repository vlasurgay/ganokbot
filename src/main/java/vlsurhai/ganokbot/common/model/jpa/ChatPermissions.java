package vlsurhai.ganokbot.common.model.jpa;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@Table(name = "chat_permissions")
public class ChatPermissions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_id", nullable = false, unique = true)
    private Long chatId;

    @Column(name = "is_frozen", nullable = false)
    private Boolean isFrozen = false;

    private Boolean canSendMessages;
    private Boolean canSendPhotos;
    private Boolean canSendVideos;
    private Boolean canSendAudios;
    private Boolean canSendDocuments;
    private Boolean canSendVideoNotes;
    private Boolean canSendVoiceNotes;
    private Boolean canSendPolls;
    private Boolean canSendOtherMessages;
    private Boolean canAddWebPagePreviews;
    private Boolean canChangeInfo;
    private Boolean canInviteUsers;
    private Boolean canPinMessages;
    private Boolean canManageTopics;
    private Boolean canSendMediaMessages;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public ChatPermissions(Long chatId) {
        this.chatId = chatId;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}