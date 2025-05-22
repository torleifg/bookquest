insert into autocomplete (suggestion, suggestion_type)
select distinct suggestion, suggestion_type
from (
         select contributor ->> 'name' as suggestion, 'contributor'::suggestion_type AS suggestion_type
         from book, lateral jsonb_array_elements(metadata -> 'contributors') as contributor
         where deleted is false and contributor ->> 'name' is not null

         union

         select metadata ->> 'title' as suggestion, 'title'::suggestion_type AS suggestion_type
         from book
         where deleted is false and metadata ->> 'title' is not null) all_suggestions
    on conflict do nothing;