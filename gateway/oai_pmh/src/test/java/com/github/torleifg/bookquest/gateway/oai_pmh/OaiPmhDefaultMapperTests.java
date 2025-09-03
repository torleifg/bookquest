package com.github.torleifg.bookquest.gateway.oai_pmh;

import com.github.torleifg.bookquest.core.domain.BookFormat;
import com.github.torleifg.bookquest.core.domain.Language;
import com.github.torleifg.bookquest.core.domain.Role;
import org.junit.jupiter.api.Test;
import org.marc4j.marc.impl.ControlFieldImpl;
import org.marc4j.marc.impl.DataFieldImpl;
import org.marc4j.marc.impl.RecordImpl;
import org.marc4j.marc.impl.SubfieldImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OaiPmhDefaultMapperTests {
    final OaiPmhDefaultMapper mapper = new OaiPmhDefaultMapper();

    @Test
    void mapRecordTest() {
        var fixedLengthDataElements = new ControlFieldImpl("008", "211210r20222022no     eo||||||0| 1 nob|d");

        var isbn = new DataFieldImpl("020", ' ', ' ');
        isbn.addSubfield(new SubfieldImpl('a', "isbn"));
        isbn.addSubfield(new SubfieldImpl('q', "innbundet"));

        var entry = new DataFieldImpl("100", '1', ' ');
        entry.addSubfield(new SubfieldImpl('4', "aut"));
        entry.addSubfield(new SubfieldImpl('4', "ill"));
        entry.addSubfield(new SubfieldImpl('a', "lastname, firstname"));

        var title = new DataFieldImpl("245", '1', '0');
        title.addSubfield(new SubfieldImpl('a', "title"));
        title.addSubfield(new SubfieldImpl('b', "remainder of title"));

        var publisher = new DataFieldImpl("264", ' ', '1');
        publisher.addSubfield(new SubfieldImpl('b', "publisher"));

        var publishedYear = new DataFieldImpl("264", ' ', '1');
        publishedYear.addSubfield(new SubfieldImpl('c', "1970"));

        var description = new DataFieldImpl("520", ' ', ' ');
        description.addSubfield(new SubfieldImpl('a', "description"));

        var language = new DataFieldImpl("041", '0', ' ');
        language.addSubfield(new SubfieldImpl('a', "eng"));

        var aboutNob = new DataFieldImpl("650", '2', '7');
        aboutNob.addSubfield(new SubfieldImpl('0', "id"));
        aboutNob.addSubfield(new SubfieldImpl('2', "aja"));
        aboutNob.addSubfield(new SubfieldImpl('9', "nob"));
        aboutNob.addSubfield(new SubfieldImpl('a', "about"));

        var aboutNno = new DataFieldImpl("650", '2', '7');
        aboutNno.addSubfield(new SubfieldImpl('0', "id"));
        aboutNno.addSubfield(new SubfieldImpl('2', "aja"));
        aboutNno.addSubfield(new SubfieldImpl('9', "nno"));
        aboutNno.addSubfield(new SubfieldImpl('a', "about"));

        var genreNob = new DataFieldImpl("655", ' ', '7');
        genreNob.addSubfield(new SubfieldImpl('0', "id"));
        genreNob.addSubfield(new SubfieldImpl('2', "ntsf"));
        genreNob.addSubfield(new SubfieldImpl('9', "nob"));
        genreNob.addSubfield(new SubfieldImpl('a', "genre"));

        var genreEng = new DataFieldImpl("655", ' ', '7');
        genreEng.addSubfield(new SubfieldImpl('0', "id"));
        genreEng.addSubfield(new SubfieldImpl('2', "ntsf"));
        genreEng.addSubfield(new SubfieldImpl('9', "eng"));
        genreEng.addSubfield(new SubfieldImpl('a', "genre"));

        var thumbnailUrl = new DataFieldImpl("856", '4', '1');
        thumbnailUrl.addSubfield(new SubfieldImpl('u', "http://thumbnailUrl"));

        var record = new RecordImpl();
        record.getControlFields().add(fixedLengthDataElements);
        record.getDataFields().addAll(List.of(isbn, title, publisher, entry, publishedYear, description, language, aboutNob, aboutNno, genreNob, genreEng, thumbnailUrl));

        var book = mapper.from("id", record);
        assertEquals("aja", book.getSource());
        assertEquals("id", book.getExternalId());
        assertFalse(book.isDeleted());

        var metadata = book.getMetadata();
        assertEquals("isbn", metadata.getIsbn());
        assertEquals("title : remainder of title", metadata.getTitle());
        assertEquals("publisher", metadata.getPublisher());
        assertEquals(2, metadata.getContributors().getFirst().roles().size());
        assertEquals(Role.AUT, metadata.getContributors().getFirst().roles().getFirst());
        assertEquals(Role.ILL, metadata.getContributors().getFirst().roles().getLast());
        assertEquals("lastname, firstname", metadata.getContributors().getFirst().name());
        assertEquals("1970", metadata.getPublishedYear());
        assertEquals("description", metadata.getDescription());
        assertEquals(2, metadata.getLanguages().size());
        assertEquals(Language.NOB, metadata.getLanguages().getFirst());
        assertEquals(Language.ENG, metadata.getLanguages().getLast());
        assertEquals(BookFormat.HARDCOVER, metadata.getFormat());
        assertEquals(2, metadata.getAbout().size());
        assertEquals("id", metadata.getAbout().getFirst().id());
        assertEquals("aja", metadata.getAbout().getFirst().source());
        assertEquals("nob", metadata.getAbout().getFirst().language());
        assertEquals("about", metadata.getAbout().getFirst().term());
        assertEquals("id", metadata.getAbout().getLast().id());
        assertEquals("aja", metadata.getAbout().getLast().source());
        assertEquals("nno", metadata.getAbout().getLast().language());
        assertEquals("about", metadata.getAbout().getLast().term());
        assertEquals(2, metadata.getGenreAndForm().size());
        assertEquals("id", metadata.getGenreAndForm().getFirst().id());
        assertEquals("ntsf", metadata.getGenreAndForm().getFirst().source());
        assertEquals("nob", metadata.getGenreAndForm().getFirst().language());
        assertEquals("genre", metadata.getGenreAndForm().getFirst().term());
        assertEquals("id", metadata.getGenreAndForm().getLast().id());
        assertEquals("ntsf", metadata.getGenreAndForm().getLast().source());
        assertEquals("eng", metadata.getGenreAndForm().getLast().language());
        assertEquals("genre", metadata.getGenreAndForm().getLast().term());
        assertEquals("http://thumbnailUrl", metadata.getThumbnailUrl().toString());
    }

    @Test
    void mapDeletedRecordTest() {
        var metadata = mapper.from("id");

        assertTrue(metadata.isDeleted());
    }
}
