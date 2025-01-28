package com.github.torleifg.semanticsearch.gateway.bibbi;

import com.github.torleifg.semanticsearch.book.service.MetadataDTO;
import no.bs.bibliografisk.model.*;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

class BibbiDefaultMapper implements BibbiMapper {

    @Override
    public MetadataDTO from(String id) {
        final MetadataDTO metadata = new MetadataDTO();
        metadata.setExternalId(id);
        metadata.setDeleted(true);

        return metadata;
    }

    @Override
    public MetadataDTO from(GetV1PublicationsHarvest200ResponsePublicationsInner publication) {
        final MetadataDTO metadata = new MetadataDTO();
        metadata.setExternalId(publication.getId());
        metadata.setDeleted(false);

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

            final List<MetadataDTO.Contributor.Role> roles = entry.getValue().stream()
                    .map(Creator::getRole)
                    .filter(Objects::nonNull)
                    .map(Creator.RoleEnum::name)
                    .map(role -> {
                        try {
                            return MetadataDTO.Contributor.Role.valueOf(role);
                        } catch (IllegalArgumentException e) {
                            return MetadataDTO.Contributor.Role.OTH;
                        }
                    })
                    .distinct()
                    .toList();

            if (roles.isEmpty()) {
                continue;
            }

            metadata.getContributors().add(new MetadataDTO.Contributor(roles, name));
        }

        if (isNotBlank(publication.getDatePublished())) {
            metadata.setPublishedYear(publication.getDatePublished());
        }

        if (isNotBlank(publication.getDescription())) {
            metadata.setDescription(publication.getDescription());
        }

        if (publication.getBookFormat() != null) {
            switch (publication.getBookFormat()) {
                case EBOOK -> metadata.setFormat(MetadataDTO.BookFormat.EBOOK);
                case AUDIOBOOKFORMAT -> metadata.setFormat(MetadataDTO.BookFormat.AUDIOBOOK);
                case HARDCOVER -> metadata.setFormat(MetadataDTO.BookFormat.HARDCOVER);
                case PAPERBACK -> metadata.setFormat(MetadataDTO.BookFormat.PAPERBACK);
                default -> metadata.setFormat(MetadataDTO.BookFormat.UNKNOWN);
            }
        } else {
            metadata.setFormat(MetadataDTO.BookFormat.UNKNOWN);
        }

        for (final Subject about : publication.getAbout()) {
            final SubjectName name = about.getName();

            final String id = about.getId();
            final String source = about.getVocabulary().getValue();

            metadata.getAbout().add(new MetadataDTO.Classification(id, source, "nob", name.getNob()));

            if (name.getNno() != null) {
                metadata.getAbout().add(new MetadataDTO.Classification(id, source, "nno", name.getNno()));
            }
        }

        for (final Genre genre : publication.getGenre()) {
            final GenreName name = genre.getName();

            final String id = genre.getId();

            final String source = Optional.ofNullable(genre.getVocabulary())
                    .map(Genre.VocabularyEnum::getValue)
                    .orElse(null);

            metadata.getGenreAndForm().addAll(List.of(
                    new MetadataDTO.Classification(id, source, "nob", name.getNob()),
                    new MetadataDTO.Classification(id, source, "nno", name.getNno())
            ));

            if (name.getEng() != null) {
                metadata.getGenreAndForm().add(new MetadataDTO.Classification(id, source, "eng", name.getEng()));
            }
        }

        Optional.ofNullable(publication.getImage())
                .map(PublicationImage::getThumbnailUrl)
                .ifPresent(metadata::setThumbnailUrl);

        return metadata;
    }
}
