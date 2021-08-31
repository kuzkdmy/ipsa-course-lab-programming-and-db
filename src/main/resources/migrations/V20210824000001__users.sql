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
