package com.github.torleifg.semanticsearch.book.service;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class BookMapperTests {
    final BookMapper bookMapper = new BookMapper();

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

        var book = bookMapper.toBook(dto);

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
}
