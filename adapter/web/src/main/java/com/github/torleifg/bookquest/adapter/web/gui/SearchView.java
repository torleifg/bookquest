package com.github.torleifg.bookquest.adapter.web.gui;

import lombok.Data;

import java.util.List;

@Data
public class SearchView {
    private String isbn;
    private String title;
    private String publisher;
    private List<ContributorView> contributors;
    private String publishedYear;
    private String description;
    private String languages;
    private String bookFormat;
    private String about;
    private String genreAndForm;
    private String thumbnailUrl;
}
