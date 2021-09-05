CREATE TYPE sex AS ENUM ('male', 'female');

CREATE TABLE users
(
    id               bigserial primary key,
    first_name       text not null,
    last_name        text not null,
    middle_name      text,
    passport_number  text,
    sex              sex  not null,
    birth_date       date not null,
    parent_id        bigint,
    hotel_preference text
);

CREATE TABLE tourist_groups
(
    id         bigserial primary key,
    group_day  date not null,
    group_name text not null
);

CREATE TABLE tourist_visit_type
(
    id         bigserial primary key,
    name       text not null,
    categories json not null
);

CREATE TABLE tourist_group_users
(
    id         bigserial primary key,
    group_id   bigint not null,
    user_id    bigint not null,
    visit_type bigint not null,
    FOREIGN KEY (group_id) REFERENCES tourist_groups (id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (visit_type) REFERENCES tourist_visit_type (id)
);

CREATE UNIQUE INDEX ON tourist_group_users (group_id, user_id);

CREATE TABLE representatives
(
    id      bigserial primary key,
    name    text not null,
    address text not null
);

CREATE TABLE excursion_agency
(
    id      bigserial primary key,
    name    text not null,
    address text not null
);

CREATE TABLE excursions
(
    id                  bigserial primary key,
    representative_id   bigint not null,
    excursion_agency_id bigint not null,
    name                text   not null,
    description         text   not null,
    scheduler           json   not null,
    FOREIGN KEY (representative_id) REFERENCES representatives (id),
    FOREIGN KEY (excursion_agency_id) REFERENCES excursion_agency (id)
);
CREATE INDEX ON excursions (representative_id);

CREATE TABLE user_excursions
(
    id             bigserial primary key,
    user_id        bigint not null,
    group_id       bigint not null,
    excursion_id   bigint not null,
    excursion_day  date   not null,
    excursion_info json,
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (group_id) REFERENCES tourist_groups (id),
    FOREIGN KEY (excursion_id) REFERENCES excursions (id)
);

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

CREATE TABLE representative_warehouses
(
    id                bigserial primary key,
    representative_id bigint not null,
    name              text,
    FOREIGN KEY (representative_id) REFERENCES representatives (id)
);

CREATE TABLE representative_user_declaration
(
    id                bigserial primary key,
    representative_id bigint         not null,
    user_id           bigint         not null,
    group_id          bigint         not null,
    is_marked         boolean default false,
    is_weighted       boolean default false,
    is_packaged       boolean default false,
    total_slots       integer        not null,
    total_weight      integer        not null,
    package_price     decimal(12, 2) not null,
    insurance_price   decimal(12, 2) not null,
    total_price       decimal(12, 2) not null,
    FOREIGN KEY (representative_id) REFERENCES representatives (id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (group_id) REFERENCES tourist_groups (id)
);

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

