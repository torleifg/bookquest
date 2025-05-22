create type suggestion_type as enum ('contributor', 'title');

create table if not exists autocomplete
(
    suggestion text not null,
    suggestion_type suggestion_type not null,
    primary key (suggestion, suggestion_type)
);

create extension if not exists pg_trgm;

create index if not exists autocomplete_suggestion_idx
    on autocomplete using gin (suggestion gin_trgm_ops);
