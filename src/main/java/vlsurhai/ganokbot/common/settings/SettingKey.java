package vlsurhai.ganokbot.common.settings;

import lombok.Getter;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import static vlsurhai.ganokbot.common.Constants.*;


@Getter
public enum SettingKey {
    RULES(
            "rules",
            RULES_CAPTION,
            SettingType.TEXT,
            null,
            null
    ),
    SPAM_FILTER_ENABLED(
            "spam_filter_enabled",
            SPAM_FILTER_ENABLED_CAPTION,
            SettingType.BOOLEAN,
            null,
            null
    ),
    SPAM_MUTE_DURATION(
            "spam_mute_duration",
            SPAM_MUTE_DURATION_CAPTION,
            SettingType.DURATION,
            "15m",
            SettingKey.SPAM_FILTER_ENABLED
    ),
    CAPTCHA_ENABLED(
            "captcha_enabled",
            CAPTCHA_ENABLED_CAPTION,
            SettingType.BOOLEAN,
            null,
            null
    ),
    COMPLAIN_ENABLED(
            "complain_enabled",
            COMPLAIN_ENABLED_CAPTION,
            SettingType.BOOLEAN,
            null,
            null
    ),
    COMPLAINTS_TO_MUTE(
            "complaints_to_mute",
            COMPLAINTS_TO_MUTE_CAPTION,
            SettingType.INTEGER,
            String.valueOf(5),
            SettingKey.COMPLAIN_ENABLED
    );

    private final String key;
    private final String caption;
    private final SettingType type;
    private final String defaultValue;
    private final SettingKey dependsOn;

    SettingKey(String key, String caption, SettingType type, String defaultValue, SettingKey dependsOn) {
        this.key = key;
        this.caption = caption;
        this.type = type;
        this.defaultValue = defaultValue;
        this.dependsOn = dependsOn;
    }

    public boolean isSkippable() {
        return this.dependsOn == null;
    }

    public boolean hasDefaultValue() {
        return this.defaultValue != null;
    }

    public static Queue<SettingKey> asQueue() {
        return new ArrayDeque<>(List.of(
                RULES,
                SPAM_FILTER_ENABLED,
                SPAM_MUTE_DURATION,
                CAPTCHA_ENABLED,
                COMPLAIN_ENABLED,
                COMPLAINTS_TO_MUTE
        ));
    }
}