CREATE INDEX book_metadata_fts_idx ON book USING GIN (to_tsvector('simple', metadata::text));
