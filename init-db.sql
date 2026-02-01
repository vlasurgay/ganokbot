
CREATE TABLE IF NOT EXISTS chats (
    chat_id BIGINT PRIMARY KEY,
    type VARCHAR(20) DEFAULT 'group'
);

CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT PRIMARY KEY,
    is_bot BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS chat_members (
    id BIGSERIAL PRIMARY KEY,
    chat_id BIGINT NOT NULL REFERENCES chats(chat_id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    status VARCHAR(20) DEFAULT 'member',
    joined_at TIMESTAMP DEFAULT NOW(),
    UNIQUE (chat_id, user_id)
);

CREATE TABLE IF NOT EXISTS chat_permissions (
    id BIGSERIAL PRIMARY KEY,
    chat_id BIGINT NOT NULL REFERENCES chats(chat_id) ON DELETE CASCADE,
    is_frozen BOOLEAN,
    can_send_messages BOOLEAN,
    can_send_photos BOOLEAN,
    can_send_videos BOOLEAN,
    can_send_audios BOOLEAN,
    can_send_documents BOOLEAN,
    can_send_video_notes BOOLEAN,
    can_send_voice_notes BOOLEAN,
    can_send_polls BOOLEAN,
    can_send_other_messages BOOLEAN,
    can_add_web_page_previews BOOLEAN,
    can_change_info BOOLEAN,
    can_invite_users BOOLEAN,
    can_pin_messages BOOLEAN,
    can_manage_topics BOOLEAN,
    can_send_media_messages BOOLEAN,
    updated_at TIMESTAMP DEFAULT NOW(),
    UNIQUE (chat_id)
);

CREATE TABLE IF NOT EXISTS complaints (
    id BIGSERIAL PRIMARY KEY,
    chat_id BIGINT NOT NULL REFERENCES chats(chat_id),
    target_user_id BIGINT NOT NULL REFERENCES users(user_id),
    initiator_id BIGINT REFERENCES users(user_id),
    expires_at TIMESTAMPTZ NOT NULL,
    UNIQUE(chat_id, target_user_id, initiator_id)

);

CREATE TABLE IF NOT EXISTS chat_settings (
    id BIGSERIAL PRIMARY KEY,
    chat_id BIGINT NOT NULL REFERENCES chats(chat_id) ON DELETE CASCADE,
    key TEXT NOT NULL,
    value TEXT,
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE (chat_id, key)
);


CREATE TABLE IF NOT EXISTS captcha_states (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(user_id),
    chat_id BIGINT NOT NULL REFERENCES chats(chat_id),
    captcha_text VARCHAR(10) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    attempts INTEGER DEFAULT 0,
    UNIQUE (user_id, chat_id)
);

CREATE TABLE IF NOT EXISTS scheduled_actions (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(32) NOT NULL,
    user_id BIGINT NOT NULL REFERENCES users(user_id),
    chat_id BIGINT NOT NULL REFERENCES chats(chat_id),
    message_id BIGINT,
    execute_at TIMESTAMPTZ NOT NULL
);

