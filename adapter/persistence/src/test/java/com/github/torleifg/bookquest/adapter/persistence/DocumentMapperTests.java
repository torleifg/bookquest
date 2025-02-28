package com.github.torleifg.bookquest.adapter.persistence;

import com.github.torleifg.bookquest.application.domain.Book;
import com.github.torleifg.bookquest.application.domain.Classification;
import com.github.torleifg.bookquest.application.domain.Contributor;
import com.github.torleifg.bookquest.application.domain.Metadata;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DocumentMapperTests {

    final DocumentMapper metadataDTOMapper = new DocumentMapper();

    @Test
    void mapDocumentTest() {
        var book = new Book();
        book.setMetadata(new Metadata());

        var document = metadataDTOMapper.toDocument(book);

        assertNotNull(document.getId());
        assertNotNull(document.getText());
        assertNotNull(document.getMetadata());
    }

    @Test
    void mapDocumentContentFromTitleTest() {
        var book = new Book();

        var metadata = new Metadata();
        metadata.setTitle("title");
        metadata.setDescription("description");
        metadata.setContributors(List.of(new Contributor(List.of(Contributor.Role.AUT), "author")));
        metadata.setAbout(List.of(new Classification("id", "source", "language", "term")));
        metadata.setGenreAndForm(List.of(new Classification("id", "source", "language", "term")));

        book.setMetadata(metadata);

        var document = metadataDTOMapper.toDocument(book);

        assertEquals("passage: Title: title. Author: author. About: term. Genre: term", document.getText());
    }

    @Test
    void mapDocumentContentFromDescriptionTest() {
        var book = new Book();

        var metadata = new Metadata();
        metadata.setTitle("title");
        metadata.setDescription("description a b c d e f g h i j k l m n o p q r s t u u v w x y");
        metadata.setContributors(List.of(new Contributor(List.of(Contributor.Role.AUT), "author"), new Contributor(List.of(Contributor.Role.ILL), "illustator")));
        metadata.setAbout(List.of(new Classification("id", "source", "language", "term1"), new Classification("id", "source", "language", "term2")));
        metadata.setGenreAndForm(List.of(new Classification("id", "source", "language", "term")));

        book.setMetadata(metadata);

        var document = metadataDTOMapper.toDocument(book);

        assertEquals("passage: Title: title. Author: author. Description: description a b c d e f g h i j k l m n o p q r s t u u v w x y. About: term1, term2. Genre: term", document.getText());
    }
}
