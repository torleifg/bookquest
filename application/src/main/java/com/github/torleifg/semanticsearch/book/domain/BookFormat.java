package com.github.torleifg.semanticsearch.book.domain;

import lombok.Getter;

@Getter
public enum BookFormat {
    AUDIOBOOK("Audiobook"),
    EBOOK("eBook"),
    HARDCOVER("Hardcover"),
    PAPERBACK("Paperback"),
    UNKNOWN("Unknown");

    private final String label;

    BookFormat(String label) {
        this.label = label;
    }
}
