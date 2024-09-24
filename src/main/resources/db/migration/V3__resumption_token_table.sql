create table resumption_token
(
    service  text not null primary key,
    created  timestamptz default now(),
    modified timestamptz default now(),
    value    text not null
);
