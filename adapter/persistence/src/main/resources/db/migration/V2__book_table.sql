create table if not exists book
(
    id              bigint generated always as identity primary key,
    external_id     text    not null,
    deleted         boolean not null default false,
    created         timestamptz      default now(),
    modified        timestamptz      default now(),
    metadata        jsonb,
    vector_store_id uuid unique references vector_store (id)
);

create unique index book_external_id_udx ON book (external_id);
