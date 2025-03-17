package com.github.torleifg.bookquest.adapter.web.gui;

import com.github.torleifg.bookquest.core.domain.*;
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
class SearchGuiMapperTests {

    @Mock
    MessageSource languageSource;

    @Mock
    MessageSource contributorSource;

    @Mock
    MessageSource formatSource;

    SearchViewMapper searchViewMapper;

    @BeforeEach
    void setUp() {
        searchViewMapper = new SearchViewMapper(languageSource, contributorSource, formatSource);
    }

    @Test
    void fromBookTest() {
        var book = new Book();

        var metadata = new Metadata();
        metadata.setIsbn("isbn");
        metadata.setTitle("title");
        metadata.setPublisher("publisher");
        metadata.setContributors(List.of(new Contributor(List.of(Role.AUT), "contributor")));
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

        var searchView = searchViewMapper.from(book, locale);

        assertEquals("isbn", searchView.getIsbn());
        assertEquals("title", searchView.getTitle());
        assertEquals("publisher", searchView.getPublisher());
        assertEquals("contributor (author)", searchView.getContributors());
        assertEquals("2025", searchView.getPublishedYear());
        assertEquals("description", searchView.getDescription());
        assertEquals("bokmål", searchView.getLanguages());
        assertEquals("innbundet", searchView.getBookFormat());
        assertEquals("om", searchView.getAbout());
        assertEquals("sjanger", searchView.getGenreAndForm());
        assertEquals("http://thumbnailUrl", searchView.getThumbnailUrl());
    }
}
