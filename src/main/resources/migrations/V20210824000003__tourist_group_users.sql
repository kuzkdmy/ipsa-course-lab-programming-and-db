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
