create or replace function search_books(search_query text, result_limit int)
    returns table
            (
                external_id     text,
                vector_store_id uuid,
                deleted         boolean,
                metadata        jsonb,
                rank            real
            ) as
$$
begin
return query
select b.external_id,
       b.vector_store_id,
       b.deleted,
       b.metadata,
       (ts_rank_cd(
               setweight(to_tsvector('simple', coalesce(b.metadata ->> 'title', '')),
                         'B') ||
               setweight(to_tsvector('simple', coalesce(jsonb_array_to_text(b.metadata -> 'contributors', 'name', 'AUT'), '')),
                         'A') ||
               setweight(to_tsvector('simple', coalesce(b.metadata ->> 'description', '')),
                         'D') ||
               setweight(to_tsvector('simple', coalesce(jsonb_array_to_text(b.metadata -> 'contributors', 'name'), '')),
                         'C') ||
               setweight(to_tsvector('simple', coalesce(jsonb_array_to_text(b.metadata -> 'about', 'term'), '')),
                         'D') ||
               setweight(to_tsvector('simple', coalesce(jsonb_array_to_text(b.metadata -> 'genreAndForm', 'term'), '')),
                         'D'),
               websearch_to_tsquery('simple', search_query)
       )
       + case
           when b.metadata->>'description' is not null and b.metadata->>'description' <> ''
           then 1 else 0
       end)::real as rank
from book b
where to_tsvector('simple',
                  coalesce(b.metadata ->> 'title', '') || ' ' ||
                  coalesce(b.metadata ->> 'description', '') || ' ' ||
                  coalesce(jsonb_array_to_text(b.metadata -> 'contributors', 'name', 'AUT'), '') || ' ' ||
                  coalesce(jsonb_array_to_text(b.metadata -> 'contributors', 'name'), '') || ' ' ||
                  coalesce(jsonb_array_to_text(b.metadata -> 'about', 'term'), '') || ' ' ||
                  coalesce(jsonb_array_to_text(b.metadata -> 'genreAndForm', 'term'), ''))
                  @@ websearch_to_tsquery('simple', search_query)
order by rank desc, b.modified desc
limit result_limit;
end;
$$
language plpgsql;
