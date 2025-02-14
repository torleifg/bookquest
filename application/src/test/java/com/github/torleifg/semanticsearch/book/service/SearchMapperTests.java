package com.github.torleifg.semanticsearch.book.service;

import com.github.torleifg.semanticsearch.book.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.net.URI;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchMapperTests {

    @Mock
    MessageSource languageSource;

    @Mock
    MessageSource contributorSource;

    @Mock
    MessageSource formatSource;

    SearchMapper searchMapper;

    @BeforeEach
    void setUp() {
        searchMapper = new SearchMapper(languageSource, contributorSource, formatSource);
    }

    @Test
    void fromBookTest() {
        var book = new Book();

        var metadata = new Metadata();
        metadata.setIsbn("isbn");
        metadata.setTitle("title");
        metadata.setPublisher("publisher");
        metadata.setContributors(List.of(new Contributor(List.of(Contributor.Role.AUT), "contributor")));
        metadata.setPublishedYear("2025");
        metadata.setDescription("description");
        metadata.setLanguages(List.of(Language.NOB));
        metadata.setFormat(BookFormat.HARDCOVER);
        metadata.setAbout(List.of(new Classification("id", "source", "nob", "om")));
        metadata.setGenreAndForm(List.of(new Classification("id", "source", "nob", "sjanger")));
        metadata.setThumbnailUrl(URI.create("http://thumbnailUrl"));

        book.setMetadata(metadata);

        var locale = Locale.of("nb");

        when(contributorSource.getMessage("AUT", null, locale)).thenReturn("author");
        when(languageSource.getMessage("NOB", null, locale)).thenReturn("bokmål");
        when(formatSource.getMessage("HARDCOVER", null, locale)).thenReturn("innbundet");

        var searchDTO = searchMapper.from(book, locale);

        assertEquals("isbn", searchDTO.getIsbn());
        assertEquals("title", searchDTO.getTitle());
        assertEquals("publisher", searchDTO.getPublisher());
        assertEquals("contributor (author)", searchDTO.getContributors());
        assertEquals("2025", searchDTO.getPublishedYear());
        assertEquals("description", searchDTO.getDescription());
        assertEquals("bokmål", searchDTO.getLanguages());
        assertEquals("innbundet", searchDTO.getFormat());
        assertEquals("om", searchDTO.getAbout());
        assertEquals("sjanger", searchDTO.getGenreAndForm());
        assertEquals("http://thumbnailUrl", searchDTO.getThumbnailUrl());
    }
}