package com.github.torleifg.semanticsearch.book.service;

import com.github.torleifg.semanticsearch.book.domain.Book;
import com.github.torleifg.semanticsearch.book.domain.Classification;
import com.github.torleifg.semanticsearch.book.domain.Contributor;
import com.github.torleifg.semanticsearch.book.domain.Metadata;
import org.springframework.stereotype.Component;

import java.util.List;

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

        for (final MetadataDTO.Contributor contributor : dto.getContributors()) {
            final List<Contributor.Role> roles = contributor.roles().stream()
                    .map(MetadataDTO.Contributor.Role::name)
                    .map(Contributor.Role::valueOf)
                    .toList();

            metadata.getContributors().add(new Contributor(roles, contributor.name()));
        }

        metadata.setPublishedYear(dto.getPublishedYear());
        metadata.setDescription(dto.getDescription());

        for (final MetadataDTO.Classification classification : dto.getAbout()) {
            metadata.getAbout().add(new Classification(classification.id(), classification.source(), classification.language(), classification.term()));
        }

        for (final MetadataDTO.Classification classification : dto.getGenreAndForm()) {
            metadata.getGenreAndForm().add(new Classification(classification.id(), classification.source(), classification.language(), classification.term()));
        }

        metadata.setThumbnailUrl(dto.getThumbnailUrl());

        book.setMetadata(metadata);

        return book;
    }
}
