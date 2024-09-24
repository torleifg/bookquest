package com.github.torleifg.semanticsearchonnx.gateway.oai_pmh;

import com.github.torleifg.semanticsearchonnx.book.domain.Book;
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
    public Book from(String id) {
        final Book book = new Book();
        book.setCode(id);
        book.setDeleted(true);

        return book;
    }

    @Override
    public Book from(String id, RecordType record) {
        final Book book = new Book();
        book.setCode(id);
        book.setDeleted(false);

        final Map<String, List<DataFieldType>> dataFieldsByTag = record.getDatafield().stream()
                .collect(groupingBy(DataFieldType::getTag));

        getSubfieldValue(dataFieldsByTag.getOrDefault("020", List.of()), "a")
                .findFirst()
                .ifPresent(book::setIsbn);

        final Optional<String> title = getSubfieldValue(dataFieldsByTag.getOrDefault("245", List.of()), "a")
                .findFirst();

        final Optional<String> remainderOfTitle = getSubfieldValue(dataFieldsByTag.getOrDefault("245", List.of()), "b")
                .findFirst();

        if (title.isPresent() && remainderOfTitle.isPresent()) {
            book.setTitle(title.get() + " : " + remainderOfTitle.get());
        } else title.ifPresent(book::setTitle);

        getSubfieldValue(dataFieldsByTag.getOrDefault("100", List.of()), "a", new Filter("4", "aut"))
                .forEach(book.getAuthors()::add);

        getSubfieldValue(dataFieldsByTag.getOrDefault("264", List.of()), "c")
                .findFirst()
                .ifPresent(book::setPublishedYear);

        getSubfieldValue(dataFieldsByTag.getOrDefault("520", List.of()), "a")
                .findFirst()
                .ifPresent(book::setDescription);

        getSubfieldValue(dataFieldsByTag.getOrDefault("650", List.of()), "a", new Filter("9", "nob"))
                .forEach(book.getAbout()::add);

        getSubfieldValue(dataFieldsByTag.getOrDefault("655", List.of()), "a", new Filter("9", "nob"))
                .forEach(book.getGenreAndForm()::add);

        getSubfieldValue(dataFieldsByTag.getOrDefault("856", List.of()), "u")
                .map(URI::create)
                .forEach(book::setThumbnailUrl);

        return book;
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
