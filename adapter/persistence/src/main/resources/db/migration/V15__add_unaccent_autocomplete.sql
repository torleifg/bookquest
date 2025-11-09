create extension if not exists unaccent;

alter table autocomplete add column suggestion_unaccented text;

update autocomplete set suggestion_unaccented = unaccent(suggestion);

drop index autocomplete_suggestion_idx;

create index autocomplete_suggestion_idx
    on autocomplete using gin (suggestion_unaccented gin_trgm_ops);
