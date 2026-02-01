package vlsurhai.ganokbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@Import(BotConfig.class)
@ComponentScan(basePackages = "vlsurhai.ganokbot")
@SpringBootApplication
public class GanokBotApplication {
    public static void main(String[] args) {
        SpringApplication.run(GanokBotApplication.class, args);
    }
}
