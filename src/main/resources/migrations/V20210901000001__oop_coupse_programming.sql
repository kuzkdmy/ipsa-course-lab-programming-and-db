CREATE TABLE hotel_starts_categories
(
    id          bigserial primary key,
    stars       int  not null,
    description text not null,
    region      text not null
);

CREATE TABLE locations
(
    id            bigserial primary key,
    name          text not null,
    location_type text not null,
    parent_id     bigint,
    FOREIGN KEY (parent_id) REFERENCES locations (id)
);
CREATE INDEX ON locations (parent_id);

CREATE TABLE travel_agent
(
    id     bigserial primary key,
    name   text   not null,
    photos text[] not null
);
CREATE TABLE travel_agent_locations
(
    id              bigserial primary key,
    travel_agent_id bigint not null,
    location_id     bigint not null,
    FOREIGN KEY (travel_agent_id) REFERENCES travel_agent (id),
    FOREIGN KEY (location_id) REFERENCES locations (id)
);
create unique index travel_agent_locations_idx on travel_agent_locations using btree (travel_agent_id, location_id);

CREATE TABLE travel_agent_categories
(
    id                     bigserial primary key,
    travel_agent_id        bigint not null,
    hotel_star_category_id bigint not null,
    FOREIGN KEY (travel_agent_id) REFERENCES travel_agent (id),
    FOREIGN KEY (hotel_star_category_id) REFERENCES hotel_starts_categories (id)
);
create unique index travel_agent_categories_idx on travel_agent_categories using btree (travel_agent_id, hotel_star_category_id);

CREATE TABLE travel_voucher
(
    id                 bigserial primary key,
    travel_agent_id    bigint    not null,
    created_at         timestamp not null,
    updated_at         timestamp not null,
    voucher_start_date timestamp not null,
    voucher_end_date   timestamp not null,
    users              json[]    not null,
    FOREIGN KEY (travel_agent_id) REFERENCES travel_agent (id)
);
