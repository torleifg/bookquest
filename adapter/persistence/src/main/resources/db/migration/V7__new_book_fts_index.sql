drop index book_metadata_fts_idx;

create
or replace function jsonb_array_to_text(jsonb_array jsonb, key_name text)
    returns text
    language sql
    immutable
as
$$
select string_agg(value, ' ')
from (
         -- Handle objects
         select element ->> key_name as value
         from jsonb_array_elements(jsonb_array) as element
         union all

         -- Handle nested objects
         select nested_element ->> key_name as value
         from jsonb_array_elements(jsonb_array) as element, jsonb_array_elements(element -> 'names') as nested_element) as combined_texts;
$$;

create index book_metadata_fts_idx on book
    using gin (to_tsvector('simple',
    coalesce (metadata ->> 'title', '') || ' ' ||
    coalesce (metadata ->> 'description', '') || ' ' ||
    coalesce (jsonb_array_to_text(metadata -> 'contributors', 'name'), '') || ' ' ||
    coalesce (jsonb_array_to_text(metadata -> 'about', 'text'), '') || ' ' ||
    coalesce (jsonb_array_to_text(metadata -> 'genreAndForm', 'text'), '')
    ));
