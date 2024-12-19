package com.github.torleifg.semanticsearch.adapter.persistence;

import com.github.torleifg.semanticsearch.book.domain.Book;
import com.github.torleifg.semanticsearch.book.domain.Metadata;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

@Component
class DocumentMapper {

    Document toDocument(Book book) {
        final Metadata metadata = book.getMetadata();

        final String passage;

        if (hasMoreThanTwentyWords(metadata.getDescription())) {
            passage = "passage: " + metadata.getDescription();
        } else {
            passage = "passage: " + metadata.getTitle();
        }

        return new Document(passage);
    }


    private boolean hasMoreThanTwentyWords(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }

        final String[] words = text.trim().split("\\s+");

        return words.length > 20;
    }
}
