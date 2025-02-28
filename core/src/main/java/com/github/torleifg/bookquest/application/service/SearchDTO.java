package com.github.torleifg.bookquest.application.service;

import lombok.Data;

@Data
public class SearchDTO {
    private String isbn;
    private String title;
    private String publisher;
    private String contributors;
    private String publishedYear;
    private String description;
    private String languages;
    private String format;
    private String about;
    private String genreAndForm;
    private String thumbnailUrl;
}
