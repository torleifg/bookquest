package com.github.torleifg.semanticsearchonnx.book.service;

import com.github.torleifg.semanticsearchonnx.book.domain.Book;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DocumentMapperTests {
    DocumentMapper mapper = new DocumentMapper();

    @Test
    void mapDocumentTest() {
        var book = new Book();

        var document = mapper.from(book);

        assertNotNull(document.getId());
        assertNotNull(document.getContent());
        assertNotNull(document.getEmbedding());
        assertNotNull(document.getMetadata());
    }

    @Test
    void mapDocumentContentFromTitleTest() {
        var book = new Book();
        book.setTitle("title");
        book.setDescription("description");

        var document = mapper.from(book);

        assertEquals("passage: title", document.getContent());
    }

    @Test
    void mapDocumentContentFromTitleAndDescriptionTest() {
        var book = new Book();
        book.setTitle("title");
        book.setDescription("description a b c d e f g h i j k l m n o p q r s t u u v w x y");

        var document = mapper.from(book);

        assertEquals("passage: description a b c d e f g h i j k l m n o p q r s t u u v w x y", document.getContent());
    }

    @Test
    void mapDocumentMetadataTest() {
        var book = new Book();
        book.setIsbn("isbn");
        book.setTitle("title");
        book.setAuthors(Set.of("author"));
        book.setPublishedYear("2020");
        book.setDescription("description");
        book.setGenreAndForm(Set.of("genre"));
        book.setAbout(Set.of("about"));
        book.setThumbnailUrl(URI.create("thumbnailUrl"));

        var document = mapper.from(book);

        var documentMetadata = document.getMetadata();
        assertEquals("isbn", documentMetadata.get("isbn"));
        assertEquals("title", documentMetadata.get("title"));
        assertEquals(Set.of("author"), documentMetadata.get("authors"));
        assertEquals("2020", documentMetadata.get("publishedYear"));
        assertEquals("description", documentMetadata.get("description"));
        assertEquals(Set.of("genre"), documentMetadata.get("genre"));
        assertEquals(Set.of("about"), documentMetadata.get("about"));
        assertEquals(URI.create("thumbnailUrl"), documentMetadata.get("thumbnailUrl"));
    }
}
