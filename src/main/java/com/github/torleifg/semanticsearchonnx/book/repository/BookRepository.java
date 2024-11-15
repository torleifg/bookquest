package com.github.torleifg.semanticsearchonnx.book.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.torleifg.semanticsearchonnx.book.domain.Book;
import com.github.torleifg.semanticsearchonnx.book.domain.Metadata;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class BookRepository {
    private final JdbcClient jdbcClient;

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    public BookRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<Book> findByExternalId(String externalId) {
        return jdbcClient.sql("""
                        select * from book where external_id = ?
                        """)
                .param(externalId)
                .query(new BookRowMapper())
                .optional();
    }

    public Optional<UUID> findVectorStoreIdByExternalId(String externalId) {
        return jdbcClient.sql("""
                        select vector_store_id from book where external_id = ?
                        """)
                .param(externalId)
                .query(UUID.class)
                .optional();
    }

    public void save(Book book) {
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

    public void save(Book book, UUID vectorStoreId) {
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

    public List<Book> query(String query, int limit) {
        return jdbcClient.sql("""
                        select * from search_books(?, ?)
                        """)
                .param(query)
                .param(limit)
                .query(new BookRowMapper())
                .list();
    }

    private static class BookRowMapper implements RowMapper<Book> {
        public Book mapRow(ResultSet rs, int i) throws SQLException {
            final Book book = new Book();
            book.setExternalId(rs.getString("external_id"));
            book.setDeleted(rs.getBoolean("deleted"));
            book.setMetadata(fromBytes(rs.getBytes("metadata")));

            return book;
        }
    }

    public static PGobject toPGobject(Metadata metadata) {
        final PGobject pGobject = new PGobject();
        pGobject.setType("jsonb");

        try {
            pGobject.setValue(OBJECT_MAPPER.writeValueAsString(metadata));
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }

        return pGobject;
    }

    public static Metadata fromBytes(byte[] bytes) {
        try {
            return OBJECT_MAPPER.readValue(bytes, Metadata.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
