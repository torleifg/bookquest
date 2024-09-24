package com.github.torleifg.semanticsearchonnx.book.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.torleifg.semanticsearchonnx.book.domain.Book;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

@Repository
public class BookRepository {
    private final JdbcClient jdbcClient;

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    public BookRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<Book> findByCode(String code) {
        return jdbcClient.sql("""
                        select * from book where code = ?
                        """)
                .param(code)
                .query(Book.class)
                .optional();
    }

    public Optional<UUID> findVectorStoreIdByCode(String code) {
        return jdbcClient.sql("""
                        select vector_store_id from book where code = ?
                        """)
                .param(code)
                .query(UUID.class)
                .optional();
    }

    public void save(Book book) {
        jdbcClient.sql("""
                        insert into book(code, payload) values (?, ?)
                        on conflict (code)
                        do update set (modified, payload) =
                        (now(), excluded.payload)
                        """)
                .param(book.getCode())
                .param(getPGobject(book))
                .update();
    }

    public void save(Book book, UUID vectorStoreId) {
        jdbcClient.sql("""
                        insert into book(code, payload, vector_store_id) values (?, ?, ?)
                        on conflict (code)
                        do update set (modified, payload, vector_store_id) =
                        (now(), excluded.payload, excluded.vector_store_id)
                        """)
                .param(book.getCode())
                .param(getPGobject(book))
                .param(vectorStoreId)
                .update();
    }

    public static PGobject getPGobject(Book book) {
        final PGobject pGobject = new PGobject();
        pGobject.setType("jsonb");

        try {
            pGobject.setValue(OBJECT_MAPPER.writeValueAsString(book));

            return pGobject;
        } catch (SQLException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
