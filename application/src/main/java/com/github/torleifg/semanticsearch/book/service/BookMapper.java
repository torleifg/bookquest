package com.github.torleifg.semanticsearch.book.service;

import com.github.torleifg.semanticsearch.book.domain.Book;
import com.github.torleifg.semanticsearch.book.domain.Metadata;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {

    public Book toBook(MetadataDTO dto) {
        final Book book = new Book();
        book.setExternalId(dto.getExternalId());
        book.setDeleted(dto.isDeleted());

        final Metadata metadata = new Metadata();
        metadata.setIsbn(dto.getIsbn());
        metadata.setTitle(dto.getTitle());
        metadata.setPublisher(dto.getPublisher());
        metadata.setAuthors(dto.getAuthors());
        metadata.setTranslators(dto.getTranslators());
        metadata.setIllustrators(dto.getIllustrators());
        metadata.setPublishedYear(dto.getPublishedYear());
        metadata.setDescription(dto.getDescription());
        metadata.setAbout(dto.getAbout());
        metadata.setGenreAndForm(dto.getGenreAndForm());
        metadata.setThumbnailUrl(dto.getThumbnailUrl());

        book.setMetadata(metadata);

        return book;
    }
}
