package vlsurhai.ganokbot.reply;

import org.telegram.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.abilitybots.api.objects.Reply;
import org.telegram.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.objects.Update;

public abstract class AbstractReply implements AbilityExtension {

    protected abstract void getAction(Update update, BaseAbilityBot bot);

    protected abstract boolean getCondition(Update update);

    public Reply buildReply() {
        return Reply.of(
                (bot, update) -> getAction(update, bot),
                this::getCondition, this::isNotCommand
        );
    }

    protected boolean isNotCommand(Update update) {
        return !update.hasMessage() || (update.hasMessage() && !update.getMessage().isCommand());
    }
}