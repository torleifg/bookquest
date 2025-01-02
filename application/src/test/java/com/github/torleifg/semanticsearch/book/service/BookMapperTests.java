package com.github.torleifg.semanticsearch.book.service;

import com.github.torleifg.semanticsearch.book.domain.Metadata;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;

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
        dto.setContributors(List.of(new MetadataDTO.Contributor(List.of(MetadataDTO.Contributor.Role.AUT), "Author")));
        dto.setPublishedYear("2020");
        dto.setDescription("description");
        dto.setGenreAndForm(List.of("genre"));
        dto.setAbout(List.of("about"));
        dto.setThumbnailUrl(URI.create("thumbnailUrl"));

        var book = bookMapper.toBook(dto);

        assertEquals("externalId", book.getExternalId());
        assertFalse(book.isDeleted());

        var metadata = book.getMetadata();

        assertEquals("isbn", metadata.getIsbn());
        assertEquals("title", metadata.getTitle());
        assertEquals("publisher", metadata.getPublisher());
        assertEquals(1, metadata.getContributors().size());
        assertEquals(1, metadata.getContributors().getFirst().roles().size());
        assertEquals(Metadata.Contributor.Role.AUT, metadata.getContributors().getFirst().roles().getFirst());
        assertEquals("Author", metadata.getContributors().getFirst().name());
        assertEquals("2020", metadata.getPublishedYear());
        assertEquals("description", metadata.getDescription());
        assertEquals(List.of("genre"), metadata.getGenreAndForm());
        assertEquals(List.of("about"), metadata.getAbout());
        assertEquals(URI.create("thumbnailUrl"), metadata.getThumbnailUrl());
    }
}
