package com.github.torleifg.semanticsearch.book.domain;

import lombok.Data;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Data
public class Metadata {
    private String isbn;
    private String title;
    private String publisher;

    private List<Contributor> contributors = new ArrayList<>();

    private String publishedYear;
    private String description;

    private List<Classification> about = new ArrayList<>();
    private List<Classification> genreAndForm = new ArrayList<>();

    private URI thumbnailUrl;

    public void languageFilter(String language) {
        about.removeIf(hasLanguage(language));
        genreAndForm.removeIf(hasLanguage(language));
    }

    private static Predicate<Classification> hasLanguage(String language) {
        return classification -> !classification.language().equals(language);
    }
}
