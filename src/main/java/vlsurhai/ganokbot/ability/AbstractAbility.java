package vlsurhai.ganokbot.ability;

import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.abilitybots.api.util.AbilityExtension;

import static org.telegram.abilitybots.api.objects.Locality.ALL;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;
import static vlsurhai.ganokbot.common.Constants.DEFAULT_INFO;

public abstract class AbstractAbility implements AbilityExtension {

    public abstract String getAbilityName();

    protected abstract void getAction(MessageContext ctx);

    public String getInfo() { return DEFAULT_INFO; }

    public Locality getLocality() { return ALL; }

    public Privacy getPrivacy() { return PUBLIC; }

    public boolean isShown() { return true; }

    public Ability buildAbility() {
        return Ability.builder()
                .name(getAbilityName())
                .info(getInfo())
                .locality(getLocality())
                .privacy(getPrivacy())
                .action(this::getAction)
                .build();
    }
}
