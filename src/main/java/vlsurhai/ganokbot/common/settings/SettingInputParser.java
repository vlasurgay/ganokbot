package vlsurhai.ganokbot.common.settings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vlsurhai.ganokbot.common.i18n.MessageResolver;
import vlsurhai.ganokbot.common.utils.Utils;

import java.time.Duration;

import static vlsurhai.ganokbot.common.Constants.NO;
import static vlsurhai.ganokbot.common.Constants.YES;

@Component
public class SettingInputParser {

    @Autowired
    private MessageResolver messageResolver;

    public String parse(String input, SettingKey key) {
        return switch (key.getType()) {
            case TEXT -> parseText(input);
            case BOOLEAN -> parseBoolean(input);
            case DURATION -> parseDuration(input);
            case INTEGER -> parsePositiveInteger(input);
        };
    }

    private String parseText(String input) {
        return input != null && !input.trim().isEmpty() ? input : null;
    }


    private String parseBoolean(String input) {

        if (messageResolver.getMessage(YES).equalsIgnoreCase(input)) {
            return String.valueOf(true);
        } else if (messageResolver.getMessage(NO).equalsIgnoreCase(input)) {
            return String.valueOf(false);
        }
        return null;
    }

    private String parseDuration(String input) {
        Duration duration = Utils.parseDuration(input);
        if (duration == null || duration.getSeconds() <= 0){
            return null;
        }
        return String.valueOf(duration.getSeconds());
    }

    private String parsePositiveInteger(String input) {
        try {
            int value = Integer.parseInt(input);
            if (value > 0) {
                return String.valueOf(value);
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }
}
