CREATE TABLE ref_hotel_stars_categories
(
    id          serial primary key,
    stars       int  not null,
    description text not null,
    region      text not null
);

CREATE TYPE location_type AS ENUM ('city', 'country', 'region');

CREATE TABLE locations
(
    id            bigserial primary key,
    name          text          not null,
    location_type location_type not null,
    parent_id     bigint,
    FOREIGN KEY (parent_id) REFERENCES locations (id)
);
CREATE INDEX ON locations (parent_id);

CREATE TABLE travel_agent
(
    id                  bigserial primary key,
    first_name          text   not null,
    last_name           text   not null,
    country             text   not null,
    city                text   not null,
    photos              text[] not null,
    hotel_star_category int    not null,
    FOREIGN KEY (hotel_star_category) REFERENCES ref_hotel_stars_categories (id)
);
