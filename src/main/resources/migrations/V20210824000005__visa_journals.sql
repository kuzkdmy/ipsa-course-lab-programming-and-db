CREATE TYPE visa_event_type AS ENUM (
    'requested',
    'approved',
    'rejected',
    'visa_received',
    'need_extra_info',
    'already_exists'
    );

CREATE TABLE visa_journal
(
    id             bigserial primary key,
    user_id        bigint    not null,
    group_id       bigint    not null,
    documents_path text[],
    event_type     visa_event_type,
    comment        text,
    event_date     timestamp not null default now(),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (group_id) REFERENCES tourist_groups (id)
);
