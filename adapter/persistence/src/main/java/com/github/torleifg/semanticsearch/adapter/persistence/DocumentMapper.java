package com.github.torleifg.semanticsearch.adapter.persistence;

import com.github.torleifg.semanticsearch.book.domain.Book;
import com.github.torleifg.semanticsearch.book.domain.Classification;
import com.github.torleifg.semanticsearch.book.domain.Contributor;
import com.github.torleifg.semanticsearch.book.domain.Metadata;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.stream.Collectors.joining;

@Component
class DocumentMapper {

    Document toDocument(Book book) {
        final Metadata metadata = book.getMetadata();

        final StringBuilder passage = new StringBuilder("passage: " + "Title: " + metadata.getTitle());

        if (!metadata.getContributors().isEmpty()) {
            passage.append(". Author: ").append(getAuthorsAsString(metadata.getContributors()));
        }

        if (hasMoreThanTwentyWords(metadata.getDescription())) {
            passage.append(". Description: ").append(metadata.getDescription());
        }

        if (!metadata.getAbout().isEmpty()) {
            passage.append(". About: ").append(getClassificationAsString(metadata.getAbout()));
        }

        if (!metadata.getGenreAndForm().isEmpty()) {
            passage.append(". Genre: ").append(getClassificationAsString(metadata.getGenreAndForm()));
        }

        return new Document(passage.toString());
    }


    private boolean hasMoreThanTwentyWords(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }

        final String[] words = text.trim().split("\\s+");

        return words.length > 20;
    }

    private String getAuthorsAsString(List<Contributor> contributors) {
        return contributors.stream()
                .filter(contributor -> contributor.roles().contains(Contributor.Role.AUT))
                .map(Contributor::name)
                .collect(joining(", "));
    }

    private String getClassificationAsString(List<Classification> classifications) {
        return classifications.stream()
                .map(Classification::term)
                .collect(joining(", "));
    }
}
