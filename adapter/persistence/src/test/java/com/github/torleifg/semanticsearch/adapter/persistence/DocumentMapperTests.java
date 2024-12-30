package com.github.torleifg.semanticsearch.adapter.persistence;

import com.github.torleifg.semanticsearch.book.domain.Book;
import com.github.torleifg.semanticsearch.book.domain.Metadata;
import org.junit.jupiter.api.Test;

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

        book.setMetadata(metadata);

        var document = metadataDTOMapper.toDocument(book);

        assertEquals("passage: title", document.getText());
    }

    @Test
    void mapDocumentContentFromDescriptionTest() {
        var book = new Book();

        var metadata = new Metadata();
        metadata.setTitle("title");
        metadata.setDescription("description a b c d e f g h i j k l m n o p q r s t u u v w x y");

        book.setMetadata(metadata);

        var document = metadataDTOMapper.toDocument(book);

        assertEquals("passage: description a b c d e f g h i j k l m n o p q r s t u u v w x y", document.getText());
    }
}
