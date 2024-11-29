package com.github.torleifg.semanticsearchonnx.book.service;

import com.github.torleifg.semanticsearchonnx.book.domain.Book;
import com.github.torleifg.semanticsearchonnx.book.domain.Metadata;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class MetadataMapper {

    public Book toBook(MetadataDTO dto) {
        final Book book = new Book();
        book.setExternalId(dto.getExternalId());
        book.setDeleted(dto.isDeleted());

        final Metadata metadata = new Metadata();
        metadata.setIsbn(dto.getIsbn());
        metadata.setTitle(dto.getTitle());
        metadata.setAuthors(dto.getAuthors());
        metadata.setTranslators(dto.getTranslators());
        metadata.setPublishedYear(dto.getPublishedYear());
        metadata.setDescription(dto.getDescription());
        metadata.setAbout(dto.getAbout());
        metadata.setGenreAndForm(dto.getGenreAndForm());
        metadata.setThumbnailUrl(dto.getThumbnailUrl());

        book.setMetadata(metadata);

        return book;
    }

    public Document toDocument(Book book) {
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
        if (isBlank(text)) {
            return false;
        }

        final String[] words = text.trim().split("\\s+");

        return words.length > 20;
    }
}
