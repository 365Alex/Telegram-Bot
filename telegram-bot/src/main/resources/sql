-- changeset developer:1
CREATE TABLE notification_task (
    id BIGSERIAL PRIMARY KEY,
    chat_id BIGINT NOT NULL,
    message_text TEXT NOT NULL,
    notification_date_time TIMESTAMP NOT NULL
);

-- changeset developer:2
CREATE INDEX idx_notification_task_datetime
ON notification_task(notification_date_time);