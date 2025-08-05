package com.github.torleifg.bookquest.core.domain;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class LanguageTests {

    @Test
    void fromTagTest() {
        var language = Language.fromTag("nob");

        assertEquals(Language.NOB, language);
    }

    @Test
    void fromTagUndeterminedTest() {
        var language = Language.fromTag("abcdefgh");

        assertEquals(Language.UND, language);
    }

    @Test
    void fromLocaleTest() {
        var language = Language.fromLocale(Locale.of("nob"));

        assertEquals(Language.NOB, language);
    }

    @Test
    void fromLocaleUndeterminedTest() {
        var language = Language.fromLocale(Locale.of("abcdefgh"));

        assertEquals(Language.UND, language);
    }
}