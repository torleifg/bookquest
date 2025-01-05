package com.github.torleifg.semanticsearch.book.service;

import com.github.torleifg.semanticsearch.book.domain.Contributor;
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
        dto.setAbout(List.of(new MetadataDTO.Classification("id", "source", "nob", "about")));
        dto.setGenreAndForm(List.of(new MetadataDTO.Classification("id", "source", "nob", "genre")));
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
        assertEquals(Contributor.Role.AUT, metadata.getContributors().getFirst().roles().getFirst());
        assertEquals("Author", metadata.getContributors().getFirst().name());
        assertEquals("2020", metadata.getPublishedYear());
        assertEquals("description", metadata.getDescription());
        assertEquals(1, metadata.getAbout().size());
        assertEquals("id", metadata.getAbout().getFirst().id());
        assertEquals("source", metadata.getAbout().getFirst().source());
        assertEquals("nob", metadata.getAbout().getFirst().language());
        assertEquals("about", metadata.getAbout().getFirst().term());
        assertEquals(1, metadata.getGenreAndForm().size());
        assertEquals("id", metadata.getGenreAndForm().getFirst().id());
        assertEquals("source", metadata.getGenreAndForm().getFirst().source());
        assertEquals("nob", metadata.getGenreAndForm().getFirst().language());
        assertEquals("genre", metadata.getGenreAndForm().getFirst().term());
        assertEquals(URI.create("thumbnailUrl"), metadata.getThumbnailUrl());
    }
}
