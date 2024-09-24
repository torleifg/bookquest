package com.github.torleifg.semanticsearchonnx.gateway.bibbi;

import com.github.torleifg.semanticsearchonnx.book.domain.Book;
import no.bs.bibliografisk.model.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
@ConditionalOnProperty(prefix = "bibbi", name = "mapper", havingValue = "default", matchIfMissing = true)
class BibbiDefaultMapper implements BibbiMapper {

    @Override
    public Book from(String id) {
        final Book book = new Book();
        book.setCode(id);
        book.setDeleted(true);

        return book;
    }

    @Override
    public Book from(GetV1PublicationsHarvest200ResponsePublicationsInner publication) {
        final Book book = new Book();
        book.setCode(publication.getId());
        book.setDeleted(false);

        if (isNotBlank(publication.getIsbn())) {
            book.setIsbn(publication.getIsbn());
        }

        if (isNotBlank(publication.getName())) {
            book.setTitle(publication.getName());
        }

        Stream.ofNullable(publication.getCreator())
                .flatMap(List::stream)
                .filter(creator -> creator.getRole() == Creator.RoleEnum.AUT)
                .map(Creator::getName)
                .forEach(book.getAuthors()::add);

        if (isNotBlank(publication.getDatePublished())) {
            book.setPublishedYear(publication.getDatePublished());
        }

        if (isNotBlank(publication.getDescription())) {
            book.setDescription(publication.getDescription());
        }

        Stream.ofNullable(publication.getGenre())
                .flatMap(List::stream)
                .map(Genre::getName)
                .map(GenreName::getNob)
                .forEach(book.getGenreAndForm()::add);

        Stream.ofNullable(publication.getAbout())
                .flatMap(List::stream)
                .map(Subject::getName)
                .map(SubjectName::getNob)
                .forEach(book.getAbout()::add);

        Optional.ofNullable(publication.getImage())
                .map(PublicationImage::getThumbnailUrl)
                .ifPresent(book::setThumbnailUrl);

        return book;
    }
}
