package com.github.torleifg.bookquest.adapter.web.gui;

import lombok.Data;

@Data
public class SearchView {
    private String isbn;
    private String title;
    private String publisher;
    private String contributors;
    private String publishedYear;
    private String description;
    private String languages;
    private String bookFormat;
    private String about;
    private String genreAndForm;
    private String thumbnailUrl;
}
