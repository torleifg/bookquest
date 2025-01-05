package com.github.torleifg.semanticsearch.gateway.bibbi;

import com.github.torleifg.semanticsearch.book.service.MetadataDTO;
import no.bs.bibliografisk.model.*;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BibbiDefaultMapperTests {
    final BibbiDefaultMapper mapper = new BibbiDefaultMapper();

    @Test
    void mapPublicationTest() {
        var publication = new GetV1PublicationsHarvest200ResponsePublicationsInner();
        publication.setId("id");
        publication.setIsbn("isbn");
        publication.setName("title");
        publication.setPublisher("publisher");

        var author = new Creator();
        author.setRole(Creator.RoleEnum.AUT);
        author.setName("creator");

        var illustrator = new Creator();
        illustrator.setRole(Creator.RoleEnum.ILL);
        illustrator.setName("creator");

        publication.setCreator(List.of(author, illustrator));

        publication.setDatePublished("1970");
        publication.setDescription("description");

        var aboutName = new SubjectName();
        aboutName.setNob("about");
        aboutName.setNno("about");

        var about = new Subject();
        about.setId("id");
        about.setVocabulary(Subject.VocabularyEnum.BIBBI);
        about.setName(aboutName);

        publication.setAbout(List.of(about));

        var genreName = new GenreName();
        genreName.setNob("genre");
        genreName.setNno("genre");
        genreName.setEng("genre");

        var genre = new Genre();
        genre.setId("id");
        genre.setVocabulary(Genre.VocabularyEnum.NTSF);
        genre.setName(genreName);

        publication.setGenre(List.of(genre));

        var publicationImage = new PublicationImage();
        publicationImage.setThumbnailUrl(URI.create("http://thumbnailUrl"));

        publication.setImage(publicationImage);

        var metadata = mapper.from(publication);

        assertEquals("id", metadata.getExternalId());
        assertEquals("isbn", metadata.getIsbn());
        assertEquals("title", metadata.getTitle());
        assertEquals("publisher", metadata.getPublisher());
        assertEquals(1, metadata.getContributors().size());
        assertEquals(2, metadata.getContributors().getFirst().roles().size());
        assertEquals(MetadataDTO.Contributor.Role.AUT, metadata.getContributors().getFirst().roles().getFirst());
        assertEquals(MetadataDTO.Contributor.Role.ILL, metadata.getContributors().getFirst().roles().getLast());
        assertEquals("creator", metadata.getContributors().getFirst().name());
        assertEquals("1970", metadata.getPublishedYear());
        assertEquals("description", metadata.getDescription());
        assertEquals(2, metadata.getAbout().size());
        assertEquals("id", metadata.getAbout().getFirst().id());
        assertEquals("bibbi", metadata.getAbout().getFirst().source());
        assertEquals("nob", metadata.getAbout().getFirst().language());
        assertEquals("about", metadata.getAbout().getFirst().term());
        assertEquals("id", metadata.getAbout().getLast().id());
        assertEquals("bibbi", metadata.getAbout().getLast().source());
        assertEquals("nno", metadata.getAbout().getLast().language());
        assertEquals("about", metadata.getAbout().getLast().term());
        assertEquals(3, metadata.getGenreAndForm().size());
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
    void mapDeletedPublicationTest() {
        var metadata = mapper.from("id");

        assertTrue(metadata.isDeleted());
    }
}
