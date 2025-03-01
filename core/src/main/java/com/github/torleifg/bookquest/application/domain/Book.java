package com.github.torleifg.bookquest.application.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Book {
    private String externalId;
    private String vectorStoreId;

    private boolean deleted;

    private Metadata metadata;
}
