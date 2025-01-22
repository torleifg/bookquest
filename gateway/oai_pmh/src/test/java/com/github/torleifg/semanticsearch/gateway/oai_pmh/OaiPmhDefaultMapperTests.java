package com.github.torleifg.semanticsearch.gateway.oai_pmh;

import com.github.torleifg.semanticsearch.book.service.MetadataDTO;
import org.junit.jupiter.api.Test;
import org.marc4j.marc.impl.DataFieldImpl;
import org.marc4j.marc.impl.RecordImpl;
import org.marc4j.marc.impl.SubfieldImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OaiPmhDefaultMapperTests {
    final OaiPmhDefaultMapper mapper = new OaiPmhDefaultMapper();

    @Test
    void mapRecordTest() {
        var isbn = new DataFieldImpl("020", ' ', ' ');
        isbn.addSubfield(new SubfieldImpl('a', "isbn"));

        var entry = new DataFieldImpl("100", ' ', ' ');
        entry.addSubfield(new SubfieldImpl('4', "aut"));
        entry.addSubfield(new SubfieldImpl('4', "ill"));
        entry.addSubfield(new SubfieldImpl('a', "entry"));

        var title = new DataFieldImpl("245", ' ', ' ');
        title.addSubfield(new SubfieldImpl('a', "title"));
        title.addSubfield(new SubfieldImpl('b', "remainder of title"));

        var publisher = new DataFieldImpl("264", ' ', ' ');
        publisher.addSubfield(new SubfieldImpl('b', "publisher"));

        var publishedYear = new DataFieldImpl("264", ' ', ' ');
        publishedYear.addSubfield(new SubfieldImpl('c', "1970"));

        var description = new DataFieldImpl("520", ' ', ' ');
        description.addSubfield(new SubfieldImpl('a', "description"));

        var aboutNob = new DataFieldImpl("650", ' ', ' ');
        aboutNob.addSubfield(new SubfieldImpl('0', "id"));
        aboutNob.addSubfield(new SubfieldImpl('2', "aja"));
        aboutNob.addSubfield(new SubfieldImpl('9', "nob"));
        aboutNob.addSubfield(new SubfieldImpl('a', "about"));

        var aboutNno = new DataFieldImpl("650", ' ', ' ');
        aboutNno.addSubfield(new SubfieldImpl('0', "id"));
        aboutNno.addSubfield(new SubfieldImpl('2', "aja"));
        aboutNno.addSubfield(new SubfieldImpl('9', "nno"));
        aboutNno.addSubfield(new SubfieldImpl('a', "about"));

        var genreNob = new DataFieldImpl("655", ' ', ' ');
        genreNob.addSubfield(new SubfieldImpl('0', "id"));
        genreNob.addSubfield(new SubfieldImpl('2', "ntsf"));
        genreNob.addSubfield(new SubfieldImpl('9', "nob"));
        genreNob.addSubfield(new SubfieldImpl('a', "genre"));

        var genreEng = new DataFieldImpl("655", ' ', ' ');
        genreEng.addSubfield(new SubfieldImpl('0', "id"));
        genreEng.addSubfield(new SubfieldImpl('2', "ntsf"));
        genreEng.addSubfield(new SubfieldImpl('9', "eng"));
        genreEng.addSubfield(new SubfieldImpl('a', "genre"));

        var thumbnailUrl = new DataFieldImpl("856", ' ', ' ');
        thumbnailUrl.addSubfield(new SubfieldImpl('u', "http://thumbnailUrl"));

        var record = new RecordImpl();
        record.getDataFields().addAll(List.of(isbn, title, publisher, entry, publishedYear, description, aboutNob, aboutNno, genreNob, genreEng, thumbnailUrl));

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
