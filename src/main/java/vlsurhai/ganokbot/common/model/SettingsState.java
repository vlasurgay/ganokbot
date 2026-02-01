package vlsurhai.ganokbot.common.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vlsurhai.ganokbot.common.model.jpa.ChatSetting;
import vlsurhai.ganokbot.common.settings.SettingKey;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

@Data
@Slf4j
@NoArgsConstructor
public class SettingsState {

    private Long userId;
    private Long currentChatId;
    private Long targetChatId;
    private Queue<SettingKey> steps = SettingKey.asQueue();
    private Map<String, ChatSetting> chatSettings = new HashMap<>();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public SettingsState(Long userId, Long currentChatId) {
        this.userId = userId;
        this.currentChatId = currentChatId;
    }

    public SettingKey currentStep() {
        skipStepIfNeeded();
        return steps.peek();
    }

    public void nextStep() {
        if (!steps.isEmpty()) {
            steps.poll();
        }
    }

    public void addSetting(SettingKey key, ChatSetting value) {
        chatSettings.put(key.getKey(), value);
    }

    public void skipStepIfNeeded() {
        while (true) {
            SettingKey currentStep = this.steps.peek();
            if (currentStep == null || currentStep.getDependsOn() == null) {
                break;
            }
            ChatSetting depending = chatSettings.get(currentStep.getDependsOn().getKey());
            if (depending == null || String.valueOf(false).equals(depending.getValue())) {
                log.info("Step '{}' skipped while setting up in the chat({})", currentStep.getKey(), this.currentChatId);
                steps.poll();
            } else {
                break;
            }
        }
    }
}
