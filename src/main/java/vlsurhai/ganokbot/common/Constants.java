package vlsurhai.ganokbot.common;

public class Constants {

    //      COMMANDS
    public static final String START_ABILITY = "start";
    public static final String HELP_ABILITY = "help";
    public static final String RULES_ABILITY = "rules";
    public static final String FREEZE_CHAT_ABILITY = "freeze";
    public static final String UNFREEZE_CHAT_ABILITY = "unfreeze";
    public static final String MUTE_ABILITY = "mute";
    public static final String UNMUTE_ABILITY = "unmute";
    public static final String COMPLAIN_ABILITY = "complain";
    public static final String SETUP_ABILITY = "setup";



    //      START
    public static final String START_MESSAGE = "start.message";
    public static final String START_INFO = "start.info";



    //      HELP
    public static final String HELP_HEADER_MESSAGE = "help.header.message";
    public static final String HELP_INFO = "help.info";
    public static final String HELP_SECTION_PRIVATE_MESSAGE = "help.section.private.message";
    public static final String HELP_SECTION_GROUP_MESSAGE = "help.section.group.message";



    //      EXCEPTION MESSAGES
    public static final String INCORRECT_COMMAND_EXCEPTION_MESSAGE = "incorrect-command.exception";
    public static final String ALREADY_COMPLAINED_EXCEPTION_MESSAGE = "already-complained.exception";
    public static final String INCORRECT_CAPTCHA_EXCEPTION_MESSAGE = "incorrect-captcha.exception";
    public static final String CAPTCHA_IGNORE_EXCEPTION_MESSAGE = "captcha-ignored.exception";
    public static final String NO_ADMIN_GROUPS_EXCEPTION_MESSAGE = "no-admin-groups.exception";
    public static final String COMPLAINING_DISABLED_EXCEPTION_MESSAGE = "complaining-disabled.exception";
    public static final String NO_RULES_SPECIFIED_EXCEPTION_MESSAGE = "no-rules-specified.exception";
    public static final String INVALID_INPUT_EXCEPTION_MESSAGE = "invalid-input.exception";




    //      PERMISSIONS MESSAGES
    public static final String FREEZE_CHAT_MESSAGE = "permissions.freeze-chat.message";
    public static final String UNFREEZE_CHAT_MESSAGE = "permissions.unfreeze-chat.message";
    public static final String SPAM_MUTE_MESSAGE = "permissions.spam-mute.message";
    public static final String MUTE_MESSAGE = "permissions.mute.message";
    public static final String UNMUTE_MESSAGE = "permissions.unmute.message";

    //      PERMISSIONS INFOS
    public static final String FREEZE_CHAT_INFO = "permissions.freeze-chat.info";
    public static final String UNFREEZE_CHAT_INFO = "permissions.unfreeze-chat.info";
    public static final String MUTE_INFO = "permissions.mute.info";
    public static final String UNMUTE_INFO = "permissions.unmute.info";




    //      COMPLAINTS
    public static final String COMPLAINT_MESSAGE = "complaint.accept-complaint.message";
    public static final String COMPLAIN_INFO = "complaint.info";




    //      CAPTCHA
    public static final String RESEND_CAPTCHA_CAPTION = "captcha.resend-captcha.caption";
    public static final String SEND_CAPTCHA_MESSAGE = "captcha.send-captcha.caption";
    public static final String ATTEMPTS_LEFT_MESSAGE = "captcha.attempts-left.message";




    //      SETUP
    public static final String RESETUP_MESSAGE = "setup.resetup.message";
    public static final String SKIP_CAPTION = "setup.skip.caption";
    public static final String CHOSEN_CHAT_CAPTION = "setup.chosen-chat.caption";
    public static final String DEFAULT_CAPTION = "setup.default.caption";
    public static final String SETUP_COMPLETED_CAPTION = "setup.setup-completed.caption";
    public static final String CHOOSE_GROUP_CAPTION = "setup.choose-chat.caption";
    public static final String RULES_CAPTION = "setup.ask-rules.caption";
    public static final String SPAM_FILTER_ENABLED_CAPTION = "setup.ask-spam-filter-enabled.caption";
    public static final String SPAM_MUTE_DURATION_CAPTION = "setup.ask-spam-mute-duration.caption";
    public static final String CAPTCHA_ENABLED_CAPTION = "setup.ask-captcha-enabled.caption";
    public static final String COMPLAIN_ENABLED_CAPTION = "setup.ask-complain-enabled.caption";
    public static final String COMPLAINTS_TO_MUTE_CAPTION = "setup.ask-complaints-to-mute.caption";
    public static final String SETUP_INFO = "setup.info";



    //      RULES
    public static final String RULES_INFO = "rules.info";




    //      COMMON
    public static final String DEFAULT_INFO = "common.default.info";
    public static final String YES = "common.yes.caption";
    public static final String NO = "common.no.caption";




    //      UTILS
    public static final String GIF_NAME = "ganok.gif";
    public static final String SPLIT = ":";
    public static final String SLASH = "/";
    public static final String HTML = "HTML";
    public static final String DURATION_REGEXP = "\\s+(\\d+[smhd](?:\\\\s+\\d+[smhd])?)";
    public static final String TAG_USER_FORMAT = "<a href='tg://user?id=%s'>%s</a>";
    public static final String TAG_USERNAME = "@%s";



    //      CALLBACKS
    public static final String CALLBACK_FORM_2 = "%s:%s";
    public static final String CALLBACK_FORM_3 = "%s:%s:%s";
    public static final String RECAPTCHA_CALLBACK = "recaptcha";
    public static final String SETUP_GROUP_CALLBACK = "setup_group";



    //      CHAT TYPES
    public static final String PRIVATE = "private";
    public static final String GROUP = "group";
    public static final String SUPERGROUP = "supergroup";
    public static final String CHANNEL = "channel";
}
