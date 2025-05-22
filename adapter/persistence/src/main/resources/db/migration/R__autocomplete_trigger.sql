create or replace function autocomplete_books()
returns trigger as
$$
declare
    contributor_name text;
    title text;
begin
    if TG_OP = 'INSERT' or TG_OP = 'UPDATE' then
        if new.deleted is false then
            if new.metadata ? 'contributors' then
                for contributor_name in
                    select contributor ->> 'name'
                    from jsonb_array_elements(new.metadata -> 'contributors') as contributor
                    where contributor ->> 'name' is not null
                loop
                    insert into autocomplete (suggestion, suggestion_type)
                    values (contributor_name, 'contributor'::suggestion_type)
                    on conflict do nothing;
                end loop;
            end if;

            title := new.metadata ->> 'title';
            if title is not null then
                insert into autocomplete (suggestion, suggestion_type)
                values (title, 'title'::suggestion_type)
                on conflict do nothing;
            end if;
        end if;
    end if;
    return null;
end;
$$ language plpgsql;

drop trigger if exists autocomplete_books_trigger on book;

create trigger autocomplete_books_trigger
after insert or update
on book
for each row
execute function autocomplete_books();
