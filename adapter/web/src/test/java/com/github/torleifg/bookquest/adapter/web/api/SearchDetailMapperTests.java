package com.github.torleifg.bookquest.adapter.web.api;

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
class SearchDetailMapperTests {

    @Mock
    MessageSource languageSource;

    @Mock
    MessageSource contributorSource;

    @Mock
    MessageSource formatSource;

    SearchDetailMapper searchDetailMapper;

    @BeforeEach
    void setUp() {
        searchDetailMapper = new SearchDetailMapper(languageSource, contributorSource, formatSource);
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
        metadata.setLanguages(List.of(Language.ENG));
        metadata.setFormat(BookFormat.HARDCOVER);
        metadata.setAbout(List.of(new Classification("id", "source", "eng", "about")));
        metadata.setGenreAndForm(List.of(new Classification("id", "source", "eng", "genre")));
        metadata.setThumbnailUrl(URI.create("http://thumbnailUrl"));

        book.setMetadata(metadata);

        var locale = Locale.of("en");

        when(contributorSource.getMessage("AUT", null, locale)).thenReturn("author");
        when(languageSource.getMessage("ENG", null, locale)).thenReturn("english");
        when(formatSource.getMessage("HARDCOVER", null, locale)).thenReturn("hardcover");

        var searchDetail = searchDetailMapper.from(book, locale);

        assertEquals("isbn", searchDetail.getIsbn());
        assertEquals("title", searchDetail.getTitle());
        assertEquals("publisher", searchDetail.getPublisher());
        assertEquals(1, searchDetail.getContributors().size());
        assertEquals("contributor", searchDetail.getContributors().getFirst().name());
        assertEquals(1, searchDetail.getContributors().getFirst().roles().size());
        assertEquals(Role.AUT, searchDetail.getContributors().getFirst().roles().getFirst().role());
        assertEquals("author", searchDetail.getContributors().getFirst().roles().getFirst().label());
        assertEquals("2025", searchDetail.getPublishedYear());
        assertEquals("description", searchDetail.getDescription());
        assertEquals(1, searchDetail.getLanguages().size());
        assertEquals(Language.ENG, searchDetail.getLanguages().getFirst().language());
        assertEquals("english", searchDetail.getLanguages().getFirst().label());
        assertEquals(BookFormat.HARDCOVER, searchDetail.getBookFormat().format());
        assertEquals("hardcover", searchDetail.getBookFormat().label());
        assertEquals(1, searchDetail.getAbout().size());
        assertEquals("id", searchDetail.getAbout().getFirst().id());
        assertEquals("about", searchDetail.getAbout().getFirst().term());
        assertEquals(1, searchDetail.getAbout().size());
        assertEquals("id", searchDetail.getGenreAndForm().getFirst().id());
        assertEquals("genre", searchDetail.getGenreAndForm().getFirst().term());
        assertEquals(URI.create("http://thumbnailUrl"), searchDetail.getThumbnailUrl());
    }
}
