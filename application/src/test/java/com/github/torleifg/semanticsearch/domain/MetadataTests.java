package com.github.torleifg.semanticsearch.domain;

import com.github.torleifg.semanticsearch.book.domain.Classification;
import com.github.torleifg.semanticsearch.book.domain.Metadata;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MetadataTests {

    @Test
    void shouldFilterLanguages() {
        var metadata = new Metadata();

        metadata.getAbout().addAll(List.of(
                new Classification("id", "source", "nob", "term"),
                new Classification("id", "source", "nno", "term")
        ));

        metadata.getGenreAndForm().addAll(List.of(
                new Classification("id", "source", "nob", "term"),
                new Classification("id", "source", "nno", "term")
        ));

        metadata.languageFilter("nob");

        assertEquals(1, metadata.getAbout().size());
        assertEquals("nob", metadata.getAbout().getFirst().language());
        assertEquals(1, metadata.getGenreAndForm().size());
        assertEquals("nob", metadata.getGenreAndForm().getFirst().language());
    }
}
