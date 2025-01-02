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
                .collect(groupingBy(Creator::getName, LinkedHashMap::new, toList()));

        for (final var entry : creatorsByName.entrySet()) {
            final List<MetadataDTO.Contributor.Role> roles = new ArrayList<>();
            final String name = entry.getKey();

            for (final Creator creator : entry.getValue()) {
                if (creator.getRole() != null) {
                    roles.add(MetadataDTO.Contributor.Role.valueOf(creator.getRole().name()));
                }
            }

            metadata.getContributors().add(new MetadataDTO.Contributor(roles, name));
        }

        if (isNotBlank(publication.getDatePublished())) {
            metadata.setPublishedYear(publication.getDatePublished());
        }

        if (isNotBlank(publication.getDescription())) {
            metadata.setDescription(publication.getDescription());
        }

        for (final Subject about : publication.getAbout()) {
            final SubjectName name = about.getName();

            final List<MetadataDTO.LocalizedString> names = List.of(
                    new MetadataDTO.LocalizedString("nob", name.getNob()),
                    new MetadataDTO.LocalizedString("nno", name.getNno())
            );

            metadata.getAbout().add(new MetadataDTO.Classification(about.getId(), names));
        }

        for (final Genre genre : publication.getGenre()) {
            final GenreName name = genre.getName();

            final List<MetadataDTO.LocalizedString> names = new ArrayList<>();
            names.add(new MetadataDTO.LocalizedString("nob", name.getNob()));
            names.add(new MetadataDTO.LocalizedString("nno", name.getNno()));

            if (name.getEng() != null) {
                names.add(new MetadataDTO.LocalizedString("eng", name.getEng()));
            }

            metadata.getGenreAndForm().add(new MetadataDTO.Classification(genre.getId(), names));
        }

        Optional.ofNullable(publication.getImage())
                .map(PublicationImage::getThumbnailUrl)
                .ifPresent(metadata::setThumbnailUrl);

        return metadata;
    }
}
