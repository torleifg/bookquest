drop index book_metadata_fts_idx;

create index book_metadata_fts_idx on book
    using gin (to_tsvector('simple',
    coalesce (metadata ->> 'title', '') || ' ' ||
    coalesce (metadata ->> 'description', '') || ' ' ||
    coalesce (jsonb_array_to_text(metadata -> 'contributors', 'name', 'AUT'), '') || ' ' ||
    coalesce (jsonb_array_to_text(metadata -> 'contributors', 'name'), '') || ' ' ||
    coalesce (jsonb_array_to_text(metadata -> 'about', 'term'), '') || ' ' ||
    coalesce (jsonb_array_to_text(metadata -> 'genreAndForm', 'term'), '')
    ));