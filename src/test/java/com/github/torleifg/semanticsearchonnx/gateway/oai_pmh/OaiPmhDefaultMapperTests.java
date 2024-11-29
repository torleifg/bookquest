package com.github.torleifg.semanticsearchonnx.gateway.oai_pmh;

import info.lc.xmlns.marcxchange_v1.DataFieldType;
import info.lc.xmlns.marcxchange_v1.ObjectFactory;
import info.lc.xmlns.marcxchange_v1.SubfieldatafieldType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OaiPmhDefaultMapperTests {
    OaiPmhDefaultMapper mapper = new OaiPmhDefaultMapper();

    ObjectFactory objectFactory = new ObjectFactory();

    @Test
    void mapRecordTest() {
        var record = objectFactory.createRecordType();

        var isbn = createDataField("020");
        var isbnValue = createSubfield("a", "isbn");
        isbn.getSubfield().add(isbnValue);

        var title = createDataField("245");
        var titleValue = createSubfield("a", "title");
        var remainderOfTitleValue = createSubfield("b", "remainder of title");
        title.getSubfield().addAll(List.of(titleValue, remainderOfTitleValue));

        var publisher = createDataField("264");
        var publisherValue = createSubfield("b", "publisher");
        publisher.getSubfield().add(publisherValue);

        var author = createDataField("100");
        var authorRole = createSubfield("4", "aut");
        var authorValue = createSubfield("a", "author");
        author.getSubfield().addAll(List.of(authorRole, authorValue));

        var translator = createDataField("700");
        var translatorRole = createSubfield("4", "trl");
        var translatorValue = createSubfield("a", "translator");
        translator.getSubfield().addAll(List.of(translatorRole, translatorValue));

        var publishedYear = createDataField("264");
        var publishedYearValue = createSubfield("c", "1970");
        publishedYear.getSubfield().add(publishedYearValue);

        var description = createDataField("520");
        var descriptionValue = createSubfield("a", "description");
        description.getSubfield().add(descriptionValue);

        var about = createDataField("650");
        var aboutLanguage = createSubfield("9", "nob");
        var aboutValue = createSubfield("a", "about");
        about.getSubfield().addAll(List.of(aboutLanguage, aboutValue));

        var genre = createDataField("655");
        var genreLanguage = createSubfield("9", "nob");
        var genreValue = createSubfield("a", "genre");
        genre.getSubfield().addAll(List.of(genreLanguage, genreValue));

        var thumbnailUrl = createDataField("856");
        var thumbnailUrlValue = createSubfield("u", "http://thumbnailUrl");
        thumbnailUrl.getSubfield().add(thumbnailUrlValue);

        record.getDatafield().addAll(List.of(isbn, genre, title, publisher, author, translator, publishedYear, description, about, genre, thumbnailUrl));

        var metadata = mapper.from("id", record);

        assertFalse(metadata.isDeleted());

        assertEquals("id", metadata.getExternalId());
        assertEquals("isbn", metadata.getIsbn());
        assertEquals("title : remainder of title", metadata.getTitle());
        assertEquals("publisher", metadata.getPublisher());
        assertEquals(1, metadata.getAuthors().size());
        assertEquals(1, metadata.getTranslators().size());
        assertEquals("1970", metadata.getPublishedYear());
        assertEquals("description", metadata.getDescription());
        assertEquals(1, metadata.getAbout().size());
        assertEquals(1, metadata.getGenreAndForm().size());
        assertEquals("http://thumbnailUrl", metadata.getThumbnailUrl().toString());
    }

    @Test
    void mapDeletedRecordTest() {
        var metadata = mapper.from("id");

        assertTrue(metadata.isDeleted());
    }

    DataFieldType createDataField(String number) {
        var datafield = objectFactory.createDataFieldType();
        datafield.setTag(number);

        return datafield;
    }

    SubfieldatafieldType createSubfield(String code, String value) {
        var subfield = objectFactory.createSubfieldatafieldType();
        subfield.setCode(code);
        subfield.setValue(value);

        return subfield;
    }
}
