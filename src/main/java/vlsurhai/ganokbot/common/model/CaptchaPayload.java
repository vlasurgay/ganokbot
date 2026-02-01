package vlsurhai.ganokbot.common.model;

import org.telegram.telegrambots.meta.api.objects.InputFile;

public record CaptchaPayload(String text, InputFile captcha) {
}
