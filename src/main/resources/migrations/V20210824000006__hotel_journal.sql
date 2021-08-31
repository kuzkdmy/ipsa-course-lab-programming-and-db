CREATE TYPE hotel_event_type AS ENUM (
    'booking_requested',
    'booking_approved',
    'booking_rejected',
    'booking_need_extra_info'
    );

CREATE TABLE hotel_journal
(
    id            bigserial primary key,
    user_id       bigint           not null,
    group_id      bigint           not null,
    hotel_id      text             not null,
    hotel_name    text             not null,
    hotel_address text             not null,
    hotel_room    text,
    event_type    hotel_event_type not null,
    comment       text,
    event_date    timestamp        not null default now(),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (group_id) REFERENCES tourist_groups (id)
);
