package vlsurhai.ganokbot.common.utils;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static vlsurhai.ganokbot.common.Constants.*;

public class Utils {

    public static String getHtmlMention(User user) {
        String displayName = user.getUserName() != null && !user.getUserName().isBlank()
                ? String.format(TAG_USERNAME, user.getUserName())
                : user.getFirstName();

        return String.format(TAG_USER_FORMAT, user.getId(), displayName);
    }

    public static boolean hasCallback(Update update, String callback) {
        return update.hasCallbackQuery() && update.getCallbackQuery().getData().startsWith(callback);
    }

    public static String getCallback2Value(String callbackQuery) {
        String[] parts = callbackQuery.trim().split(SPLIT);

        if (parts.length != 2) {
            return null;
        }
        return parts[1];
    }

    public static String[] getCallback3Values(String callbackQuery) {
        String[] parts = callbackQuery.trim().split(SPLIT);

        if (parts.length != 3) {
            return null;
        }
        return parts;
    }

    public static Long encode(Long userId, Long chatId) {
        return (userId << 32) ^ chatId;
    }

    public static Duration parseDuration(String raw) {
        Duration duration = Duration.ZERO;
        Pattern unitPattern = Pattern.compile("(\\d+)\\s*([smhdw])");
        Matcher matcher = unitPattern.matcher(raw);

        while (matcher.find()) {
            long value = Long.parseLong(matcher.group(1));
            char unit = matcher.group(2).charAt(0);

            duration = switch (unit) {
                case 's' -> duration.plusSeconds(value);
                case 'm' -> duration.plusMinutes(value);
                case 'h' -> duration.plusHours(value);
                case 'd' -> duration.plusDays(value);
                case 'w' -> duration.plusDays(value * 7);
                default -> duration;
            };
        }

        return duration;
    }
}
