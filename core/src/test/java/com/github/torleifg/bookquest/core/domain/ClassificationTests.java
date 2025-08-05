package com.github.torleifg.bookquest.core.domain;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassificationTests {

    @Test
    void hasLanguageMatchTest() {
        var classification = new Classification("id", "source", "nob", null, "term");

        assertTrue(classification.hasLanguage(Locale.of("nob")));
    }

    @Test
    void hasNoLanguageMatchTest() {
        var classification = new Classification("id", "source", "nob", null, "term");

        assertFalse(classification.hasLanguage(Locale.of("eng")));
    }

    @Test
    void hasLanguageTagMatchTest() {
        var classification = new Classification("id", "source", null, Language.NOB, "term");

        assertTrue(classification.hasLanguage(Locale.of("nob")));
    }

    @Test
    void hasNoLanguageTagTest() {
        var classification = new Classification("id", "source", null, Language.NOB, "term");

        assertFalse(classification.hasLanguage(Locale.of("eng")));
    }

    @Test
    void hasNoLanguageOrLanguageTagTest() {
        var classification = new Classification("id", "source", null, null, "term");

        assertFalse(classification.hasLanguage(Locale.of("eng")));
    }
}
