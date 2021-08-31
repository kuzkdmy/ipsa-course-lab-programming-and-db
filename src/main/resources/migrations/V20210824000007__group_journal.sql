CREATE TYPE tourist_group_event_type AS ENUM (
    'user_arrived',
    'user_visa_board_problem',
    'user_transferred_to_hotel'
    );

CREATE TABLE tourist_group_journal
(
    id         bigserial primary key,
    user_id    bigint                   not null,
    group_id   bigint                   not null,
    event_type tourist_group_event_type not null,
    comment    text,
    event_date timestamp                not null default now(),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (group_id) REFERENCES tourist_groups (id)
);
