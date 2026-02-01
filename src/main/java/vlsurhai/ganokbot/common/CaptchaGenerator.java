package vlsurhai.ganokbot.common;

import com.wf.captcha.GifCaptcha;
import com.wf.captcha.base.Captcha;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import vlsurhai.ganokbot.common.model.CaptchaPayload;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static vlsurhai.ganokbot.common.Constants.GIF_NAME;

@Component
public class CaptchaGenerator {

    @Value("${captcha.width:180}")
    private int width;

    @Value("${captcha.height:75}")
    private int height;

    @Value("${captcha.length:5}")
    private int length;

    public CaptchaPayload generate() throws IOException, FontFormatException {
        GifCaptcha captcha = new GifCaptcha(this.width, this.height, this.length);
        captcha.setFont(Captcha.FONT_8);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            captcha.out(outputStream);
            ByteArrayInputStream stream = new ByteArrayInputStream(outputStream.toByteArray());

            return new CaptchaPayload(captcha.text(), new InputFile(stream, GIF_NAME));
        }
    }


}
