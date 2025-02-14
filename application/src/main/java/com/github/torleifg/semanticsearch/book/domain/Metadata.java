package com.github.torleifg.semanticsearch.book.domain;

import lombok.Data;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Data
public class Metadata {
    private String isbn;
    private String title;
    private String publisher;

    private List<Contributor> contributors = new ArrayList<>();

    private String publishedYear;
    private String description;

    private List<Language> languages = new ArrayList<>();

    private BookFormat format;

    private List<Classification> about = new ArrayList<>();
    private List<Classification> genreAndForm = new ArrayList<>();

    private URI thumbnailUrl;
}
