package com.github.torleifg.semanticsearchonnx.book.repository;

import com.github.torleifg.semanticsearchonnx.TestcontainersConfig;
import com.github.torleifg.semanticsearchonnx.book.domain.Book;
import com.github.torleifg.semanticsearchonnx.book.domain.Metadata;
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
    void findByExternalIdTest() {
        var book = createBook();

        client.sql("insert into book (external_id, deleted, metadata) values (?, ?, ?)")
                .param(book.getExternalId())
                .param(book.isDeleted())
                .param(BookRepository.toPGobject(book.getMetadata()))
                .update();

        var optionalBook = repository.findByExternalId("externalId");
        assertTrue(optionalBook.isPresent());
        assertEquals(book, optionalBook.get());
    }

    @Test
    void findVectorStoreIdByExternalIdTest() {
        var book = createBook();

        var vectorStoreId = client.sql("insert into vector_store (content) values ('content') returning id")
                .query(UUID.class)
                .single();

        client.sql("insert into book (external_id, vector_store_id) values (?, ?)")
                .param(book.getExternalId())
                .param(vectorStoreId)
                .update();

        assertTrue(repository.findVectorStoreIdByExternalId("externalId").isPresent());
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

        client.sql("insert into book (external_id, metadata) values (?, ?)")
                .param(book.getExternalId())
                .param(BookRepository.toPGobject(book.getMetadata()))
                .update();

        book.getMetadata().setTitle("new title");
        repository.save(book);

        var title = client.sql("select metadata->>'title' from book where external_id = ?")
                .param(book.getExternalId())
                .query(String.class)
                .single();

        assertEquals("new title", title);
    }

    @Test
    void insertBookWithVectorStoreIdTest() {
        var book = createBook();

        var vectorStoreId = client.sql("insert into vector_store (content) values ('content') returning id")
                .query(UUID.class)
                .single();

        repository.save(book, vectorStoreId);

        var id = client.sql("select vector_store_id from book where external_id = ?")
                .param(book.getExternalId())
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

        client.sql("insert into book (external_id, vector_store_id) values (?, ?)")
                .param(book.getExternalId())
                .param(vectorStoreId)
                .update();

        var newVectorStoreId = client.sql("insert into vector_store (content) values ('new content') returning id")
                .query(UUID.class)
                .single();

        repository.save(book, newVectorStoreId);

        var id = client.sql("select vector_store_id from book where external_id = ?")
                .param(book.getExternalId())
                .query(UUID.class)
                .optional();

        assertTrue(id.isPresent());
        assertEquals(newVectorStoreId, id.get());
    }

    @Test
    void queryTest() {
        var book = createBook();

        client.sql("insert into book (external_id, metadata) values (?, ?)")
                .param(book.getExternalId())
                .param(BookRepository.toPGobject(book.getMetadata()))
                .update();

        var books = repository.query("title description", 10);
        assertEquals(1, books.size());
        assertEquals(books.getFirst().getExternalId(), book.getExternalId());
        assertEquals(books.getFirst().getMetadata(), book.getMetadata());
    }

    Book createBook() {
        var book = new Book();
        book.setExternalId("externalId");
        book.setDeleted(false);

        var metadata = new Metadata();
        metadata.setIsbn("isbn");
        metadata.setTitle("title");
        metadata.setDescription("description");
        metadata.setAbout(Set.of("about"));

        book.setMetadata(metadata);

        return book;
    }
}
