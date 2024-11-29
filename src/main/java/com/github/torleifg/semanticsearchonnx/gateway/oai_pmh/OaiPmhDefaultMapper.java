package com.github.torleifg.semanticsearchonnx.gateway.oai_pmh;

import com.github.torleifg.semanticsearchonnx.book.service.MetadataDTO;
import info.lc.xmlns.marcxchange_v1.DataFieldType;
import info.lc.xmlns.marcxchange_v1.RecordType;
import info.lc.xmlns.marcxchange_v1.SubfieldatafieldType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

@Component
@ConditionalOnProperty(prefix = "oai-pmh", name = "mapper", havingValue = "default", matchIfMissing = true)
class OaiPmhDefaultMapper implements OaiPmhMapper {

    @Override
    public MetadataDTO from(String id) {
        final MetadataDTO metadata = new MetadataDTO();
        metadata.setExternalId(id);
        metadata.setDeleted(true);

        return metadata;
    }

    @Override
    public MetadataDTO from(String id, RecordType record) {
        final MetadataDTO metadata = new MetadataDTO();
        metadata.setExternalId(id);
        metadata.setDeleted(false);

        final Map<String, List<DataFieldType>> dataFieldsByTag = record.getDatafield().stream()
                .collect(groupingBy(DataFieldType::getTag));

        getSubfieldValue(dataFieldsByTag.getOrDefault("020", List.of()), "a")
                .findFirst()
                .ifPresent(metadata::setIsbn);

        final Optional<String> title = getSubfieldValue(dataFieldsByTag.getOrDefault("245", List.of()), "a")
                .findFirst();

        final Optional<String> remainderOfTitle = getSubfieldValue(dataFieldsByTag.getOrDefault("245", List.of()), "b")
                .findFirst();

        if (title.isPresent() && remainderOfTitle.isPresent()) {
            metadata.setTitle(title.get() + " : " + remainderOfTitle.get());
        } else title.ifPresent(metadata::setTitle);

        getSubfieldValue(dataFieldsByTag.getOrDefault("264", List.of()), "b")
                .findFirst()
                .ifPresent(metadata::setPublisher);

        Stream.concat(
                getSubfieldValue(
                        dataFieldsByTag.getOrDefault("100", List.of()), "a", new Filter("4", "aut")),
                getSubfieldValue(
                        dataFieldsByTag.getOrDefault("700", List.of()), "a", new Filter("4", "aut"))
        ).forEach(metadata.getAuthors()::add);

        Stream.concat(
                getSubfieldValue(
                        dataFieldsByTag.getOrDefault("100", List.of()), "a", new Filter("4", "trl")),
                getSubfieldValue(
                        dataFieldsByTag.getOrDefault("700", List.of()), "a", new Filter("4", "trl"))
        ).forEach(metadata.getTranslators()::add);

        getSubfieldValue(dataFieldsByTag.getOrDefault("264", List.of()), "c")
                .findFirst()
                .ifPresent(metadata::setPublishedYear);

        getSubfieldValue(dataFieldsByTag.getOrDefault("520", List.of()), "a")
                .findFirst()
                .ifPresent(metadata::setDescription);

        getSubfieldValue(dataFieldsByTag.getOrDefault("650", List.of()), "a", new Filter("9", "nob"))
                .forEach(metadata.getAbout()::add);

        getSubfieldValue(dataFieldsByTag.getOrDefault("655", List.of()), "a", new Filter("9", "nob"))
                .forEach(metadata.getGenreAndForm()::add);

        getSubfieldValue(dataFieldsByTag.getOrDefault("856", List.of()), "u")
                .map(URI::create)
                .forEach(metadata::setThumbnailUrl);

        return metadata;
    }

    private static Stream<String> getSubfieldValue(List<DataFieldType> dataFieldTypes, String subfieldCode) {
        return dataFieldTypes.stream()
                .map(DataFieldType::getSubfield)
                .flatMap(List::stream)
                .filter(subfield -> subfield.getCode().equals(subfieldCode))
                .map(SubfieldatafieldType::getValue);
    }

    private static Stream<String> getSubfieldValue(List<DataFieldType> dataFieldTypes, String subfieldCode, Filter filter) {
        return dataFieldTypes.stream()
                .map(DataFieldType::getSubfield)
                .filter(subfields -> OaiPmhDefaultMapper.filter(subfields, filter))
                .flatMap(List::stream)
                .filter(subfield -> subfield.getCode().equals(subfieldCode))
                .map(SubfieldatafieldType::getValue);
    }

    private static boolean filter(List<SubfieldatafieldType> subfields, Filter filter) {
        return subfields.stream()
                .filter(subfield -> subfield.getCode().equals(filter.code()))
                .anyMatch(subfield -> subfield.getValue().equals(filter.value()));
    }

    record Filter(String code, String value) {
    }
}
