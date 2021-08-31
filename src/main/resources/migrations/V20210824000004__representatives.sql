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
