package com.github.torleifg.semanticsearchonnx.book.service;

import com.github.torleifg.semanticsearchonnx.book.domain.Book;
import com.github.torleifg.semanticsearchonnx.book.domain.Metadata;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MetadataMapperTests {
    MetadataMapper metadataMapper = new MetadataMapper();

    @Test
    void mapBookTest() {
        var dto = new MetadataDTO();
        dto.setExternalId("externalId");
        dto.setDeleted(false);
        dto.setIsbn("isbn");
        dto.setTitle("title");
        dto.setPublisher("publisher");
        dto.setAuthors(Set.of("author"));
        dto.setIllustrators(Set.of("illustrator"));
        dto.setTranslators(Set.of("translator"));
        dto.setPublishedYear("2020");
        dto.setDescription("description");
        dto.setGenreAndForm(Set.of("genre"));
        dto.setAbout(Set.of("about"));
        dto.setThumbnailUrl(URI.create("thumbnailUrl"));

        var book = metadataMapper.toBook(dto);

        assertEquals("externalId", book.getExternalId());
        assertFalse(book.isDeleted());

        var metadata = book.getMetadata();

        assertEquals("isbn", metadata.getIsbn());
        assertEquals("title", metadata.getTitle());
        assertEquals("publisher", metadata.getPublisher());
        assertEquals(Set.of("author"), metadata.getAuthors());
        assertEquals(Set.of("translator"), metadata.getTranslators());
        assertEquals(Set.of("illustrator"), metadata.getIllustrators());
        assertEquals("2020", metadata.getPublishedYear());
        assertEquals("description", metadata.getDescription());
        assertEquals(Set.of("genre"), metadata.getGenreAndForm());
        assertEquals(Set.of("about"), metadata.getAbout());
        assertEquals(URI.create("thumbnailUrl"), metadata.getThumbnailUrl());
    }

    @Test
    void mapDocumentTest() {
        var book = new Book();
        book.setMetadata(new Metadata());

        var document = metadataMapper.toDocument(book);

        assertNotNull(document.getId());
        assertNotNull(document.getContent());
        assertNotNull(document.getMetadata());
    }

    @Test
    void mapDocumentContentFromTitleTest() {
        var book = new Book();

        var metadata = new Metadata();
        metadata.setTitle("title");
        metadata.setDescription("description");

        book.setMetadata(metadata);

        var document = metadataMapper.toDocument(book);

        assertEquals("passage: title", document.getContent());
    }

    @Test
    void mapDocumentContentFromDescriptionTest() {
        var book = new Book();

        var metadata = new Metadata();
        metadata.setTitle("title");
        metadata.setDescription("description a b c d e f g h i j k l m n o p q r s t u u v w x y");

        book.setMetadata(metadata);

        var document = metadataMapper.toDocument(book);

        assertEquals("passage: description a b c d e f g h i j k l m n o p q r s t u u v w x y", document.getContent());
    }
}
