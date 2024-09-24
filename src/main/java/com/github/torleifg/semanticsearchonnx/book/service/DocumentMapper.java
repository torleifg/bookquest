package com.github.torleifg.semanticsearchonnx.book.service;

import com.github.torleifg.semanticsearchonnx.book.domain.Book;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class DocumentMapper {

    public Document from(Book book) {
        final String passage;

        if (hasMoreThanTwentyWords(book.getDescription())) {
            passage = "passage: " + book.getDescription();
        } else {
            passage = "passage: " + book.getTitle();
        }

        final Map<String, Object> documentMetadata = createDocumentMetadata(book);

        return new Document(passage, documentMetadata);
    }

    private static Map<String, Object> createDocumentMetadata(Book book) {
        final Map<String, Object> documentMetadata = new HashMap<>();
        documentMetadata.put("isbn", book.getIsbn());
        documentMetadata.put("title", book.getTitle());
        documentMetadata.put("authors", book.getAuthors());
        documentMetadata.put("description", book.getDescription());
        documentMetadata.put("publishedYear", book.getPublishedYear());
        documentMetadata.put("genre", book.getGenreAndForm());
        documentMetadata.put("about", book.getAbout());
        documentMetadata.put("thumbnailUrl", book.getThumbnailUrl());

        return documentMetadata;
    }

    public static boolean hasMoreThanTwentyWords(String text) {
        if (isBlank(text)) {
            return false;
        }

        final String[] words = text.trim().split("\\s+");

        return words.length > 20;
    }
}
