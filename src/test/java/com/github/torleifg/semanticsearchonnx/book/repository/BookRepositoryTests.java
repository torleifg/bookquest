package com.github.torleifg.semanticsearchonnx.book.repository;

import com.github.torleifg.semanticsearchonnx.TestcontainersConfig;
import com.github.torleifg.semanticsearchonnx.book.domain.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@Import(TestcontainersConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookRepositoryTests {

    @Autowired
    JdbcClient client;

    BookRepository repository;

    @BeforeEach
    void init() {
        repository = new BookRepository(client);
    }

    @Test
    void findByCodeTest() {
        var book = createBook();

        client.sql("insert into book (code) values (?)")
                .param(book.getCode())
                .update();

        assertTrue(repository.findByCode("code").isPresent());
    }

    @Test
    void findVectorStoreIdByCodeTest() {
        var book = createBook();

        var vectorStoreId = client.sql("insert into vector_store (content) values ('content') returning id")
                .query(UUID.class)
                .single();

        client.sql("insert into book (code, vector_store_id) values (?, ?)")
                .param(book.getCode())
                .param(vectorStoreId)
                .update();

        assertTrue(repository.findVectorStoreIdByCode("code").isPresent());
    }

    @Test
    void insertBookTest() {
        var book = createBook();

        repository.save(book);

        var count = client.sql("select count(*) from book")
                .query(Integer.class)
                .optional();

        assertTrue(count.isPresent());
        assertEquals(1, count.get());
    }

    @Test
    void updateBookTest() {
        var book = createBook();

        client.sql("insert into book (code, payload) values (?, ?)")
                .param(book.getCode())
                .param(BookRepository.getPGobject(book))
                .update();

        book.setTitle("new title");
        repository.save(book);

        var payload = client.sql("select payload->>'title' from book where code = ?")
                .param(book.getCode())
                .query(String.class)
                .single();

        assertEquals("new title", payload);
    }

    @Test
    void insertBookWithVectorStoreIdTest() {
        var book = createBook();

        var vectorStoreId = client.sql("insert into vector_store (content) values ('content') returning id")
                .query(UUID.class)
                .single();

        repository.save(book, vectorStoreId);

        var id = client.sql("select vector_store_id from book where code = ?")
                .param(book.getCode())
                .query(UUID.class)
                .optional();

        assertTrue(id.isPresent());
        assertEquals(vectorStoreId, id.get());
    }

    @Test
    void updateBookWithVectorStoreIdTest() {
        var book = createBook();

        var vectorStoreId = client.sql("insert into vector_store (content) values ('content') returning id")
                .query(UUID.class)
                .single();

        client.sql("insert into book (code, vector_store_id) values (?, ?)")
                .param(book.getCode())
                .param(vectorStoreId)
                .update();

        var newVectorStoreId = client.sql("insert into vector_store (content) values ('new content') returning id")
                .query(UUID.class)
                .single();

        repository.save(book, newVectorStoreId);

        var id = client.sql("select vector_store_id from book where code = ?")
                .param(book.getCode())
                .query(UUID.class)
                .optional();

        assertTrue(id.isPresent());
        assertEquals(newVectorStoreId, id.get());
    }

    Book createBook() {
        var book = new Book();
        book.setCode("code");
        book.setIsbn("isbn");
        book.setTitle("title");
        book.setDescription("description");
        book.setAbout(Set.of("about"));

        return book;
    }
}
