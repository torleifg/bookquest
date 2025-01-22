package com.github.torleifg.semanticsearch.gateway.oai_pmh;

import com.github.torleifg.semanticsearch.book.service.MetadataDTO;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;

import java.net.URI;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

class OaiPmhDefaultMapper implements OaiPmhMapper {

    @Override
    public MetadataDTO from(String id) {
        final MetadataDTO metadata = new MetadataDTO();
        metadata.setExternalId(id);
        metadata.setDeleted(true);

        return metadata;
    }

    @Override
    public MetadataDTO from(String id, Record record) {
        final MetadataDTO metadata = new MetadataDTO();
        metadata.setExternalId(id);
        metadata.setDeleted(false);

        final Map<String, List<DataField>> dataFieldsByTag = record.getDataFields().stream()
                .collect(groupingBy(DataField::getTag, LinkedHashMap::new, toList()));

        dataFieldsByTag.getOrDefault("020", List.of()).stream()
                .map(dataField -> dataField.getSubfield('a'))
                .filter(Objects::nonNull)
                .map(Subfield::getData)
                .findFirst()
                .ifPresent(metadata::setIsbn);

        final Optional<String> title = dataFieldsByTag.getOrDefault("245", List.of()).stream()
                .map(dataField -> dataField.getSubfield('a'))
                .filter(Objects::nonNull)
                .map(Subfield::getData)
                .findFirst();

        final Optional<String> remainderOfTitle = dataFieldsByTag.getOrDefault("245", List.of()).stream()
                .map(dataField -> dataField.getSubfield('b'))
                .filter(Objects::nonNull)
                .map(Subfield::getData)
                .findFirst();

        if (title.isPresent() && remainderOfTitle.isPresent()) {
            metadata.setTitle(title.get() + " : " + remainderOfTitle.get());
        } else title.ifPresent(metadata::setTitle);

        dataFieldsByTag.getOrDefault("264", List.of()).stream()
                .map(dataField -> dataField.getSubfield('b'))
                .filter(Objects::nonNull)
                .map(Subfield::getData)
                .findFirst()
                .ifPresent(metadata::setPublisher);

        final List<DataField> dataFields = new ArrayList<>();
        dataFields.addAll(dataFieldsByTag.getOrDefault("100", List.of()));
        dataFields.addAll(dataFieldsByTag.getOrDefault("110", List.of()));
        dataFields.addAll(dataFieldsByTag.getOrDefault("111", List.of()));
        dataFields.addAll(dataFieldsByTag.getOrDefault("700", List.of()));
        dataFields.addAll(dataFieldsByTag.getOrDefault("710", List.of()));
        dataFields.addAll(dataFieldsByTag.getOrDefault("711", List.of()));

        for (final DataField dataField : dataFields) {
            final Optional<String> name = Optional.ofNullable(dataField.getSubfield('a'))
                    .map(Subfield::getData);

            if (name.isEmpty()) {
                continue;
            }

            final List<MetadataDTO.Contributor.Role> roles = dataField.getSubfields('4').stream()
                    .filter(Objects::nonNull)
                    .map(Subfield::getData)
                    .map(role -> {
                        try {
                            return MetadataDTO.Contributor.Role.valueOf(role.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();

            if (roles.isEmpty()) {
                continue;
            }

            metadata.getContributors().add(new MetadataDTO.Contributor(roles, name.get()));
        }

        dataFieldsByTag.getOrDefault("264", List.of()).stream()
                .map(dataField -> dataField.getSubfield('c'))
                .filter(Objects::nonNull)
                .map(Subfield::getData)
                .findFirst()
                .ifPresent(metadata::setPublishedYear);

        dataFieldsByTag.getOrDefault("520", List.of()).stream()
                .map(dataField -> dataField.getSubfield('a'))
                .filter(Objects::nonNull)
                .map(Subfield::getData)
                .findFirst()
                .ifPresent(metadata::setDescription);

        dataFieldsByTag.getOrDefault("650", List.of()).stream()
                .map(OaiPmhDefaultMapper::createClassification)
                .forEach(metadata.getAbout()::add);

        dataFieldsByTag.getOrDefault("655", List.of()).stream()
                .map(OaiPmhDefaultMapper::createClassification)
                .forEach(metadata.getGenreAndForm()::add);

        dataFieldsByTag.getOrDefault("856", List.of()).stream()
                .map(dataField -> dataField.getSubfield('u'))
                .filter(Objects::nonNull)
                .map(Subfield::getData)
                .map(url -> {
                    try {
                        return URI.create(url);
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .forEach(metadata::setThumbnailUrl);

        return metadata;
    }

    private static MetadataDTO.Classification createClassification(DataField dataField) {
        final String id = Optional.ofNullable(dataField.getSubfield('0'))
                .map(Subfield::getData)
                .orElse(null);

        final String source = Optional.ofNullable(dataField.getSubfield('2'))
                .map(Subfield::getData)
                .orElse(null);

        final String language = Optional.ofNullable(dataField.getSubfield('9'))
                .map(Subfield::getData)
                .orElse("und");

        final String term = Optional.ofNullable(dataField.getSubfield('a'))
                .map(Subfield::getData)
                .orElse(null);

        return new MetadataDTO.Classification(id, source, language, term);
    }
}
