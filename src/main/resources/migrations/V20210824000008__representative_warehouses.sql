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
