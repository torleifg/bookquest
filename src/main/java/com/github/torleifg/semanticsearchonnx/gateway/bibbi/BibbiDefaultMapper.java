package com.github.torleifg.semanticsearchonnx.gateway.bibbi;

import com.github.torleifg.semanticsearchonnx.book.service.MetadataDTO;
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

        Stream.ofNullable(publication.getCreator())
                .flatMap(List::stream)
                .filter(creator -> creator.getRole() == Creator.RoleEnum.AUT)
                .map(Creator::getName)
                .forEach(metadata.getAuthors()::add);

        if (isNotBlank(publication.getDatePublished())) {
            metadata.setPublishedYear(publication.getDatePublished());
        }

        if (isNotBlank(publication.getDescription())) {
            metadata.setDescription(publication.getDescription());
        }

        Stream.ofNullable(publication.getGenre())
                .flatMap(List::stream)
                .map(Genre::getName)
                .map(GenreName::getNob)
                .forEach(metadata.getGenreAndForm()::add);

        Stream.ofNullable(publication.getAbout())
                .flatMap(List::stream)
                .map(Subject::getName)
                .map(SubjectName::getNob)
                .forEach(metadata.getAbout()::add);

        Optional.ofNullable(publication.getImage())
                .map(PublicationImage::getThumbnailUrl)
                .ifPresent(metadata::setThumbnailUrl);

        return metadata;
    }
}
