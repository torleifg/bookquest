create table if not exists book
(
    id              bigint generated always as identity primary key,
    code            text not null,
    created         timestamptz default now(),
    modified        timestamptz default now(),
    payload         jsonb,
    vector_store_id uuid unique references vector_store (id)
);

create unique index book_code_udx ON book (code);
