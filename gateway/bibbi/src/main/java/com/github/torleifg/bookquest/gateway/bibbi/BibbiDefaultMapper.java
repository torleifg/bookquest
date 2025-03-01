package com.github.torleifg.bookquest.gateway.bibbi;

import com.github.torleifg.bookquest.application.domain.*;
import no.bs.bibliografisk.model.*;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

class BibbiDefaultMapper implements BibbiMapper {

    @Override
    public Book from(String id) {
        final Book book = new Book();
        book.setExternalId(id);
        book.setDeleted(true);

        return book;
    }

    @Override
    public Book from(GetV1PublicationsHarvest200ResponsePublicationsInner publication) {
        final Book book = new Book();
        book.setExternalId(publication.getId());
        book.setDeleted(false);

        final Metadata metadata = new Metadata();

        if (isNotBlank(publication.getIsbn())) {
            metadata.setIsbn(publication.getIsbn());
        }

        if (isNotBlank(publication.getName())) {
            metadata.setTitle(publication.getName());
        }

        if (isNotBlank(publication.getPublisher())) {
            metadata.setPublisher(publication.getPublisher());
        }

        final Map<String, List<Creator>> creatorsByName = Stream.ofNullable(publication.getCreator())
                .flatMap(List::stream)
                .filter(creator -> isNotBlank(creator.getName()))
                .collect(groupingBy(Creator::getName, LinkedHashMap::new, toList()));

        for (final var entry : creatorsByName.entrySet()) {
            final String name = entry.getKey();

            final List<Contributor.Role> roles = entry.getValue().stream()
                    .map(Creator::getRole)
                    .filter(Objects::nonNull)
                    .map(Creator.RoleEnum::name)
                    .map(role -> {
                        try {
                            return Contributor.Role.valueOf(role);
                        } catch (IllegalArgumentException e) {
                            return Contributor.Role.OTH;
                        }
                    })
                    .distinct()
                    .toList();

            if (roles.isEmpty()) {
                continue;
            }

            metadata.getContributors().add(new Contributor(roles, name));
        }

        if (isNotBlank(publication.getDatePublished())) {
            metadata.setPublishedYear(publication.getDatePublished());
        }

        if (isNotBlank(publication.getDescription())) {
            metadata.setDescription(publication.getDescription());
        }

        if (isNotBlank(publication.getInLanguage())) {
            try {
                metadata.setLanguages(List.of(Language.valueOf(publication.getInLanguage().toUpperCase())));
            } catch (IllegalArgumentException ignored) {
                metadata.setLanguages(List.of(Language.UND));
            }
        }

        if (publication.getBookFormat() != null) {
            switch (publication.getBookFormat()) {
                case EBOOK -> metadata.setFormat(BookFormat.EBOOK);
                case AUDIOBOOKFORMAT -> metadata.setFormat(BookFormat.AUDIOBOOK);
                case HARDCOVER -> metadata.setFormat(BookFormat.HARDCOVER);
                case PAPERBACK -> metadata.setFormat(BookFormat.PAPERBACK);
                default -> metadata.setFormat(BookFormat.UNKNOWN);
            }
        } else {
            metadata.setFormat(BookFormat.UNKNOWN);
        }

        for (final Subject about : publication.getAbout()) {
            final SubjectName name = about.getName();

            final String id = about.getId();
            final String source = about.getVocabulary().getValue();

            metadata.getAbout().add(new Classification(id, source, "nob", name.getNob()));

            if (name.getNno() != null) {
                metadata.getAbout().add(new Classification(id, source, "nno", name.getNno()));
            }
        }

        for (final Genre genre : publication.getGenre()) {
            final GenreName name = genre.getName();

            final String id = genre.getId();

            final String source = Optional.ofNullable(genre.getVocabulary())
                    .map(Genre.VocabularyEnum::getValue)
                    .orElse(null);

            metadata.getGenreAndForm().addAll(List.of(
                    new Classification(id, source, "nob", name.getNob()),
                    new Classification(id, source, "nno", name.getNno())
            ));

            if (name.getEng() != null) {
                metadata.getGenreAndForm().add(new Classification(id, source, "eng", name.getEng()));
            }
        }

        Optional.ofNullable(publication.getImage())
                .map(PublicationImage::getThumbnailUrl)
                .ifPresent(metadata::setThumbnailUrl);

        book.setMetadata(metadata);

        return book;
    }
}
