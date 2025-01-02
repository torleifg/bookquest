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

        var aboutNob = createDataField("650");
        var aboutNobId = createSubfield("0", "id");
        var aboutNobLanguage = createSubfield("9", "nob");
        var aboutNobValue = createSubfield("a", "about");
        aboutNob.getSubfield().addAll(List.of(aboutNobId, aboutNobLanguage, aboutNobValue));

        var aboutNno = createDataField("650");
        var aboutNnoId = createSubfield("0", "id");
        var aboutNnoLanguage = createSubfield("9", "nno");
        var aboutNnoValue = createSubfield("a", "about");
        aboutNno.getSubfield().addAll(List.of(aboutNnoId, aboutNnoLanguage, aboutNnoValue));

        var genreNob = createDataField("655");
        var genreNobId = createSubfield("0", "id");
        var genreNobLanguage = createSubfield("9", "nob");
        var genreMobValue = createSubfield("a", "genre");
        genreNob.getSubfield().addAll(List.of(genreNobId, genreNobLanguage, genreMobValue));

        var genreEng = createDataField("655");
        var genreEngId = createSubfield("0", "id");
        var genreEngLanguage = createSubfield("9", "eng");
        var genreEngValue = createSubfield("a", "genre");
        genreEng.getSubfield().addAll(List.of(genreEngId, genreEngLanguage, genreEngValue));

        var thumbnailUrl = createDataField("856");
        var thumbnailUrlValue = createSubfield("u", "http://thumbnailUrl");
        thumbnailUrl.getSubfield().add(thumbnailUrlValue);

        record.getDatafield().addAll(List.of(isbn, title, publisher, entry, publishedYear, description, aboutNob, aboutNno, genreNob, genreEng, thumbnailUrl));

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
        assertEquals("id", metadata.getAbout().getFirst().id());
        assertEquals(2, metadata.getAbout().getFirst().names().size());
        assertEquals("nob", metadata.getAbout().getFirst().names().getFirst().language());
        assertEquals("about", metadata.getAbout().getFirst().names().getFirst().text());
        assertEquals("nno", metadata.getAbout().getFirst().names().getLast().language());
        assertEquals("about", metadata.getAbout().getFirst().names().getLast().text());
        assertEquals(1, metadata.getGenreAndForm().size());
        assertEquals("id", metadata.getGenreAndForm().getFirst().id());
        assertEquals(2, metadata.getGenreAndForm().getFirst().names().size());
        assertEquals("nob", metadata.getGenreAndForm().getFirst().names().getFirst().language());
        assertEquals("genre", metadata.getGenreAndForm().getFirst().names().getFirst().text());
        assertEquals("eng", metadata.getGenreAndForm().getFirst().names().getLast().language());
        assertEquals("genre", metadata.getGenreAndForm().getFirst().names().getLast().text());
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
