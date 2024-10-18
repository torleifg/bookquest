create or replace function search_books(search_query text, result_limit int)
    returns table
            (
                external_id text,
                deleted     boolean,
                metadata    jsonb,
                rank        real
            )
as
$$
begin
    return query
        select b.external_id,
               b.deleted,
               b.metadata,
               ts_rank(
                       setweight(to_tsvector('simple', coalesce(b.metadata ->> 'title', '')), 'B') ||
                       setweight(to_tsvector('simple', coalesce(b.metadata ->> 'description', '')), 'D') ||
                       setweight(to_tsvector('simple', coalesce(jsonb_array_to_text(b.metadata -> 'authors'), '')),
                                 'A') ||
                       setweight(to_tsvector('simple', coalesce(jsonb_array_to_text(b.metadata -> 'about'), '')),
                                 'C') ||
                       setweight(to_tsvector('simple', coalesce(jsonb_array_to_text(b.metadata -> 'genreandform'), '')),
                                 'C'),
                       plainto_tsquery('simple', search_query)
               ) as rank
        from book b
        where to_tsvector('simple', coalesce(b.metadata ->> 'title', '')) ||
              to_tsvector('simple', coalesce(b.metadata ->> 'description', '')) ||
              to_tsvector('simple', coalesce(jsonb_array_to_text(b.metadata -> 'authors'), '')) ||
              to_tsvector('simple', coalesce(jsonb_array_to_text(b.metadata -> 'about'), '')) ||
              to_tsvector('simple', coalesce(jsonb_array_to_text(b.metadata -> 'genreandform'), ''))
                  @@ plainto_tsquery('simple', search_query)
        order by rank desc
        limit result_limit;
end;
$$ language plpgsql;
