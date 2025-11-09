create or replace function unaccent_suggestion()
    returns trigger as $$
begin
    new.suggestion_unaccented = unaccent(new.suggestion);
    return new;
end;
$$ language plpgsql;

drop trigger if exists unaccent_suggestion_trigger on autocomplete;

create trigger unaccent_suggestion_trigger
before insert or update
on autocomplete
for each row
execute function unaccent_suggestion();