package vlsurhai.ganokbot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Slf4j
@Component
public class GanokBot extends AbilityBot {

    private final long creatorId;

    public GanokBot(@Value("${bot.token}") String token,
                    @Value("${bot.username}") String username,
                    @Value("${bot.creatorId}") Long creatorId,
                    @Autowired List<AbilityExtension> abilityList) {

        super(token, username);
        this.creatorId = creatorId;
        addExtensions(abilityList);
        log.info("Found external abilities and singleton replies: {}", abilityList.size());
    }

    @Override
    public long creatorId() {
        return creatorId;
    }

    @Override
    public void onUpdateReceived(Update update) {
        super.onUpdateReceived(update);
    }
}
