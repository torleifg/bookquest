package com.github.torleifg.bookquest.gateway.oai_pmh;

import com.github.torleifg.bookquest.core.domain.*;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;

import java.net.URI;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

class OaiPmhDefaultMapper implements OaiPmhMapper {

    @Override
    public Book from(String id) {
        final Book book = new Book();
        book.setExternalId(id);
        book.setDeleted(true);

        return book;
    }

    @Override
    public Book from(String id, Record record) {
        final Book book = new Book();
        book.setExternalId(id);
        book.setDeleted(false);

        final Metadata metadata = new Metadata();

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

            final List<Role> roles = dataField.getSubfields('4').stream()
                    .filter(Objects::nonNull)
                    .map(Subfield::getData)
                    .map(role -> {
                        try {
                            return Role.valueOf(role.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            return Role.OTH;
                        }
                    })
                    .distinct()
                    .toList();

            if (roles.isEmpty()) {
                continue;
            }

            metadata.getContributors().add(new Contributor(roles, name.get()));
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

        record.getControlFields().stream()
                .filter(controlField -> controlField.getTag().equals("008"))
                .map(ControlField::getData)
                .filter(data -> data.length() > 38)
                .map(data -> data.substring(35, 38))
                .map(Language::fromTag)
                .forEach(metadata.getLanguages()::add);

        dataFieldsByTag.getOrDefault("041", List.of()).stream()
                .map(dataField -> dataField.getSubfields('a'))
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .map(Subfield::getData)
                .map(Language::fromTag)
                .filter(language -> !metadata.getLanguages().contains(language))
                .forEach(metadata.getLanguages()::add);

        dataFieldsByTag.getOrDefault("020", List.of()).stream()
                .map(dataField -> dataField.getSubfield('q'))
                .filter(Objects::nonNull)
                .map(Subfield::getData)
                .findFirst()
                .ifPresent(format -> {
                    if (format.equals("innbundet")) {
                        metadata.setFormat(BookFormat.HARDCOVER);
                    } else if (format.equals("heftet")) {
                        metadata.setFormat(BookFormat.PAPERBACK);
                    }
                });

        dataFieldsByTag.getOrDefault("347", List.of()).stream()
                .map(dataField -> dataField.getSubfield('0'))
                .filter(Objects::nonNull)
                .map(Subfield::getData)
                .findFirst()
                .ifPresent(fileType -> {
                    if (fileType.equals("http://rdaregistry.info/termList/fileType/1001")) {
                        metadata.setFormat(BookFormat.AUDIOBOOK);
                    } else if (fileType.equals("http://rdaregistry.info/termList/fileType/1002")) {
                        metadata.setFormat(BookFormat.EBOOK);
                    }
                });

        if (metadata.getFormat() == null) {
            metadata.setFormat(BookFormat.UNKNOWN);
        }

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

        book.setMetadata(metadata);

        return book;
    }

    private static Classification createClassification(DataField dataField) {
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

        return new Classification(id, source, language, Language.fromTag(language), term);
    }
}
