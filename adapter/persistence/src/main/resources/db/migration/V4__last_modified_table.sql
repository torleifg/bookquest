create table last_modified
(
    service  text        not null primary key,
    created  timestamptz default now(),
    modified timestamptz default now(),
    value    timestamptz not null
);
