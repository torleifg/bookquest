create or replace function jsonb_array_to_text(jsonb_array jsonb)
    returns text
    language sql
    immutable
as
$$
select string_agg(element, ' ')
from (select jsonb_array_elements_text(jsonb_array) as element) as elements;
$$;

create index book_metadata_fts_idx on book
    using gin (to_tsvector('simple',
                           coalesce(metadata ->> 'title', '') || ' ' ||
                           coalesce(metadata ->> 'description', '') || ' ' ||
                           coalesce(jsonb_array_to_text(metadata -> 'authors'), '') || ' ' ||
                           coalesce(jsonb_array_to_text(metadata -> 'about'), '') || ' ' ||
                           coalesce(jsonb_array_to_text(metadata -> 'genreAndForm'), '')
               ));
