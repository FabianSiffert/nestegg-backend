create table asset (
    id         uuid primary key,
    name       varchar(200) not null,
    type       varchar(50)  not null,
    currency   varchar(3)   not null,
    notes      text,
    created_at timestamptz  not null default now()
);

create table price_point (
    id         uuid primary key,
    asset_id   uuid           not null references asset (id) on delete cascade,
    date       date           not null,
    unit_price numeric(24, 8) not null,
    note       text,
    created_at timestamptz    not null default now(),
    unique (asset_id, date)
);
