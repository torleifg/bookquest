package com.github.torleifg.semanticsearch.book.domain;

import lombok.Data;

@Data
public class Book {
    private String externalId;
    private String vectorStoreId;

    private boolean deleted;

    private Metadata metadata;

    public Book() {
    }
}
