package com.github.torleifg.bookquest.adapter.persistence;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.torleifg.bookquest.core.domain.Book;
import com.github.torleifg.bookquest.core.domain.Metadata;
import com.github.torleifg.bookquest.core.repository.BookRepository;
import org.postgresql.util.PGobject;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

@Repository
class BookRepositoryAdapter implements BookRepository {
    private final JdbcClient jdbcClient;
    private final VectorStore vectorStore;

    private final DocumentMapper documentMapper;

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    BookRepositoryAdapter(JdbcClient jdbcClient, VectorStore vectorStore, DocumentMapper documentMapper) {
        this.jdbcClient = jdbcClient;
        this.vectorStore = vectorStore;
        this.documentMapper = documentMapper;
    }

    @Override
    @Transactional
    public void save(List<Book> books) {
        for (final Book book : books) {
            final String externalId = book.getExternalId();

            if (book.isDeleted()) {
                findByExternalId(externalId).ifPresent(existingBook -> {
                    existingBook.setDeleted(true);

                    save(existingBook);
                });

                continue;
            }

            final Optional<UUID> existingVector = findVectorStoreIdByExternalId(externalId);

            final Document document = documentMapper.toDocument(book);
            vectorStore.add(List.of(document));

            final UUID newVector = UUID.fromString(document.getId());
            save(book, newVector);

            existingVector.map(UUID::toString)
                    .map(List::of)
                    .ifPresent(vectorStore::delete);
        }
    }

    @Override
    public List<Book> lastModified(int limit) {
        return jdbcClient.sql("""
                        select * from book
                        where deleted is false
                        and metadata ->> 'description' is not null
                        and metadata ->> 'thumbnailUrl' is not null
                        order by modified desc, id limit ?
                        """)
                .param(limit)
                .query(new BookRowMapper())
                .list();
    }

    @Override
    public List<Book> fullTextSearch(String query, int limit) {
        return jdbcClient.sql("""
                        select * from search_books(?, ?)
                        where deleted is false
                        """)
                .param(query)
                .param(limit)
                .query(new BookRowMapper())
                .list();
    }

    @Override
    public List<Book> semanticSearch(String query, int limit) {
        final SearchRequest searchRequest = SearchRequest.builder()
                .query("query: " + query)
                .similarityThreshold(0.8)
                .topK(limit)
                .build();

        final List<UUID> ids = Stream.ofNullable(vectorStore.similaritySearch(searchRequest))
                .flatMap(List::stream)
                .map(Document::getId)
                .map(UUID::fromString)
                .toList();

        return asListOfBooks(ids);
    }

    @Override
    public List<Book> hybridSearch(String query, int limit) {
        final Map<Book, Double> rrf = new ReciprocalRankFusion(query, fullTextSearch(query, limit), semanticSearch(query, limit))
                .compute();

        return rrf.keySet()
                .stream()
                .limit(limit)
                .toList();
    }

    @Override
    public List<Book> semanticSimilarity(int limit) {
        final Optional<Document> randomDocument = jdbcClient.sql("""
                        select * from vector_store order by random() limit 1
                        """)
                .query((resultSet, rowNum) -> new Document(resultSet.getString("content")))
                .optional();

        return semanticSimilarity(randomDocument, limit);
    }

    @Override
    public List<Book> semanticSimilarity(String isbn, int limit) {
        final Optional<Document> document = jdbcClient.sql("""
                        select * from vector_store
                        join book on book.vector_store_id = vector_store.id
                        where book.metadata ->> 'isbn' = ?
                        limit 1
                        """)
                .param(isbn)
                .query((resultSet, rowNum) -> new Document(resultSet.getString("content")))
                .optional();

        return semanticSimilarity(document, limit);
    }

    private List<Book> semanticSimilarity(Optional<Document> document, int limit) {
        if (document.isEmpty()) {
            return List.of();
        }

        final SearchRequest searchRequest = SearchRequest.builder()
                .query("passage: " + document.get().getText())
                .similarityThreshold(0.8)
                .topK(limit)
                .build();

        final List<UUID> ids = Stream.ofNullable(vectorStore.similaritySearch(searchRequest))
                .flatMap(List::stream)
                .map(Document::getId)
                .map(UUID::fromString)
                .toList();

        return asListOfBooks(ids);
    }

    private List<Book> asListOfBooks(List<UUID> ids) {
        final Map<String, Book> books = findByVectorStoreIdsIn(ids).stream()
                .collect(toMap(Book::getVectorStoreId, Function.identity()));

        return ids.stream()
                .map(UUID::toString)
                .map(books::get)
                .filter(Objects::nonNull)
                .toList();
    }

    Optional<Book> findByExternalId(String externalId) {
        return jdbcClient.sql("""
                        select * from book where external_id = ?
                        """)
                .param(externalId)
                .query(new BookRowMapper())
                .optional();
    }

    Optional<UUID> findVectorStoreIdByExternalId(String externalId) {
        return jdbcClient.sql("""
                        select vector_store_id from book where external_id = ?
                        """)
                .param(externalId)
                .query(UUID.class)
                .optional();
    }

    List<Book> findByVectorStoreIdsIn(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        final String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));

        final String query = String.format("select * from book where deleted is false and vector_store_id in (%s)", placeholders);

        return jdbcClient.sql(query)
                .params(ids.toArray())
                .query(new BookRowMapper())
                .list();
    }

    void save(Book book) {
        jdbcClient.sql("""
                        insert into book(external_id, deleted, metadata) values (?, ?, ?)
                        on conflict (external_id)
                        do update set (modified, deleted, metadata) =
                        (now(), excluded.deleted, excluded.metadata)
                        """)
                .param(book.getExternalId())
                .param(book.isDeleted())
                .param(toPGobject(book.getMetadata()))
                .update();
    }

    void save(Book book, UUID vectorStoreId) {
        jdbcClient.sql("""
                        insert into book(external_id, deleted, metadata, vector_store_id) values (?, ?, ?, ?)
                        on conflict (external_id)
                        do update set (modified, deleted, metadata, vector_store_id) =
                        (now(), excluded.deleted, excluded.metadata, excluded.vector_store_id)
                        """)
                .param(book.getExternalId())
                .param(book.isDeleted())
                .param(toPGobject(book.getMetadata()))
                .param(vectorStoreId)
                .update();
    }

    private static class BookRowMapper implements RowMapper<Book> {
        public Book mapRow(ResultSet rs, int i) throws SQLException {
            final Book book = new Book();
            book.setExternalId(rs.getString("external_id"));
            book.setVectorStoreId(rs.getString("vector_store_id"));
            book.setDeleted(rs.getBoolean("deleted"));
            book.setMetadata(fromPGobject(rs.getBytes("metadata")));

            return book;
        }
    }

    static PGobject toPGobject(Metadata metadata) {
        final PGobject pGobject = new PGobject();
        pGobject.setType("jsonb");

        try {
            pGobject.setValue(OBJECT_MAPPER.writeValueAsString(metadata));
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }

        return pGobject;
    }

    static Metadata fromPGobject(byte[] bytes) {
        try {
            return OBJECT_MAPPER.readValue(bytes, Metadata.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
