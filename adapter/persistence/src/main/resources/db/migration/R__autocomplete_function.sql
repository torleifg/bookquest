drop function if exists autocomplete_books(text, int);

create function autocomplete_books(search_query text, result_limit int)
    returns table
            (
                suggestion text
            ) as
$$
declare
pattern text := '%' || search_query || '%';
begin
return query
select suggestion_val as suggestion
from (select distinct contributor ->> 'name' as suggestion_val, 1 as order_col
      from book, lateral jsonb_array_elements(metadata -> 'contributors') as contributor
      where contributor ->> 'name' ilike pattern and deleted is false

      union

      select distinct metadata ->> 'title' as suggestion_val, 2 as order_col
      from book
      where metadata ->> 'title' ilike pattern and deleted is false) as combined
order by order_col, suggestion_val limit result_limit;
end;
$$
language plpgsql;
