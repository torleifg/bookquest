package com.github.torleifg.semanticsearch.adapter.persistence;

import com.github.torleifg.semanticsearch.book.domain.Book;
import com.github.torleifg.semanticsearch.book.domain.Metadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@JdbcTest
@Import(TestcontainersConfig.class)
@ContextConfiguration(classes = BookRepositoryAdapter.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookRepositoryAdapterTests {

    @Autowired
    JdbcClient client;

    @MockitoBean
    VectorStore vectorStore;

    @MockitoBean
    DocumentMapper documentMapper;

    BookRepositoryAdapter adapter;

    @BeforeEach
    void init() {
        adapter = new BookRepositoryAdapter(client, vectorStore, documentMapper);

        client.sql("truncate table book cascade").update();
    }

    @Test
    void findByExternalIdTest() {
        var book = createBook();

        client.sql("insert into book (external_id, deleted, metadata) values (?, ?, ?)")
                .param(book.getExternalId())
                .param(book.isDeleted())
                .param(BookRepositoryAdapter.toPGobject(book.getMetadata()))
                .update();

        var optionalBook = adapter.findByExternalId("externalId");
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

        assertTrue(adapter.findVectorStoreIdByExternalId("externalId").isPresent());
    }

    @Test
    void findByVectorStoreIdsInTest() {
        var book = createBook();

        var vectorStoreId = client.sql("insert into vector_store (content) values ('content') returning id")
                .query(UUID.class)
                .single();

        client.sql("insert into book (external_id, vector_store_id, metadata) values (?, ?, ?)")
                .param(book.getExternalId())
                .param(vectorStoreId)
                .param(BookRepositoryAdapter.toPGobject(book.getMetadata()))
                .update();

        assertEquals(1, adapter.findByVectorStoreIdsIn(List.of(vectorStoreId, UUID.randomUUID())).size());
    }

    @Test
    void insertBookTest() {
        var book = createBook();

        var vectorStoreId = client.sql("insert into vector_store (content) values ('description') returning id")
                .query(UUID.class)
                .single();

        var document = new Document(vectorStoreId.toString(), "description", Map.of());
        when(documentMapper.toDocument(any())).thenReturn(document);

        adapter.save(List.of(book));

        var count = client.sql("select count(*) from book")
                .query(Integer.class)
                .optional();

        assertTrue(count.isPresent());
        assertEquals(1, count.get());
    }

    @Test
    void updateBookTest() {
        var book = createBook();

        var vectorStoreId = client.sql("insert into vector_store (content) values ('description') returning id")
                .query(UUID.class)
                .single();

        var document = new Document(vectorStoreId.toString(), "description", Map.of());
        when(documentMapper.toDocument(any())).thenReturn(document);

        client.sql("insert into book (external_id, metadata) values (?, ?)")
                .param(book.getExternalId())
                .param(BookRepositoryAdapter.toPGobject(book.getMetadata()))
                .update();

        book.getMetadata().setTitle("new title");
        adapter.save(List.of(book));

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

        adapter.save(book, vectorStoreId);

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

        adapter.save(book, newVectorStoreId);

        var id = client.sql("select vector_store_id from book where external_id = ?")
                .param(book.getExternalId())
                .query(UUID.class)
                .optional();

        assertTrue(id.isPresent());
        assertEquals(newVectorStoreId, id.get());
    }

    @Test
    void lastModifiedTest() {
        var firstBook = createBook();

        var secondBook = createBook();
        secondBook.setExternalId("secondExternalId");
        secondBook.getMetadata().setDescription(null);

        for (final var book : List.of(firstBook, secondBook)) {
            client.sql("insert into book (external_id, metadata) values (?, ?)")
                    .param(book.getExternalId())
                    .param(BookRepositoryAdapter.toPGobject(book.getMetadata()))
                    .update();
        }

        var books = adapter.lastModified(10);
        assertEquals(1, books.size());
        assertEquals(books.getFirst().getExternalId(), firstBook.getExternalId());
        assertEquals(books.getFirst().getMetadata(), firstBook.getMetadata());
    }

    @Test
    void fullTextSearchTest() {
        var book = createBook();

        client.sql("insert into book (external_id, metadata) values (?, ?)")
                .param(book.getExternalId())
                .param(BookRepositoryAdapter.toPGobject(book.getMetadata()))
                .update();

        var books = adapter.fullTextSearch("title description", 10);
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

        book.setMetadata(metadata);

        return book;
    }
}
