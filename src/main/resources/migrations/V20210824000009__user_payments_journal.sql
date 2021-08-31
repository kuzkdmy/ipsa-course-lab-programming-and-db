CREATE TABLE user_payments_journal
(
    id           bigserial primary key,
    user_id      bigint      not null,
    group_id     bigint      not null,
    payment_type varchar(50) not null,
    comment      text,
    event_date   timestamp   not null default now(),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (group_id) REFERENCES tourist_groups (id)
);
