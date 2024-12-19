package com.github.torleifg.semanticsearch.book.service;

import lombok.Data;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

@Data
public class MetadataDTO {
    private String externalId;
    private boolean deleted;

    private String isbn;
    private String title;
    private String publisher;

    private Set<String> authors = new HashSet<>();
    private Set<String> translators = new HashSet<>();
    private Set<String> illustrators = new HashSet<>();

    private String publishedYear;
    private String description;

    private Set<String> about = new HashSet<>();
    private Set<String> genreAndForm = new HashSet<>();

    private URI thumbnailUrl;

    public MetadataDTO() {
    }
}
