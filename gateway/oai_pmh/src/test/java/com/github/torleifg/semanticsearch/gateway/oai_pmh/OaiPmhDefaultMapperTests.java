package com.github.torleifg.semanticsearch.gateway.oai_pmh;

import com.github.torleifg.semanticsearch.book.service.MetadataDTO;
import info.lc.xmlns.marcxchange_v1.DataFieldType;
import info.lc.xmlns.marcxchange_v1.ObjectFactory;
import info.lc.xmlns.marcxchange_v1.SubfieldatafieldType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OaiPmhDefaultMapperTests {
    final OaiPmhDefaultMapper mapper = new OaiPmhDefaultMapper();

    final ObjectFactory objectFactory = new ObjectFactory();

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

        var entry = createDataField("100");
        var authorRole = createSubfield("4", "aut");
        var illustratorRole = createSubfield("4", "ill");
        var entryValue = createSubfield("a", "entry");
        entry.getSubfield().addAll(List.of(authorRole, illustratorRole, entryValue));

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

        record.getDatafield().addAll(List.of(isbn, title, publisher, entry, publishedYear, description, about, genre, thumbnailUrl));

        var metadata = mapper.from("id", record);

        assertFalse(metadata.isDeleted());

        assertEquals("id", metadata.getExternalId());
        assertEquals("isbn", metadata.getIsbn());
        assertEquals("title : remainder of title", metadata.getTitle());
        assertEquals("publisher", metadata.getPublisher());
        assertEquals(2, metadata.getContributors().getFirst().roles().size());
        assertEquals(MetadataDTO.Contributor.Role.AUT, metadata.getContributors().getFirst().roles().getFirst());
        assertEquals(MetadataDTO.Contributor.Role.ILL, metadata.getContributors().getFirst().roles().getLast());
        assertEquals("entry", metadata.getContributors().getFirst().name());
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
