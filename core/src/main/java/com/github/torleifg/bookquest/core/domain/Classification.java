package com.github.torleifg.bookquest.core.domain;

import java.util.Locale;

public record Classification(String id, String source, String language, Language languageTag, String term) {

    public boolean hasLanguage(Locale locale) {
        final Language languageFromLocale = Language.fromLocale(locale);

        if (languageTag == languageFromLocale) {
            return true;
        }

        if (language == null) {
            return false;
        }

        return Language.fromTag(language) == languageFromLocale;
    }
}
