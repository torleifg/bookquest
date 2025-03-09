package com.github.torleifg.bookquest.application;

import com.github.torleifg.bookquest.application.domain.Book;
import com.github.torleifg.bookquest.application.domain.Metadata;
import com.github.torleifg.bookquest.application.service.BookService;
import com.github.torleifg.bookquest.application.service.MetadataGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HarvesterTests {

    @Mock
    MetadataGateway metadataGateway;

    @Mock
    BookService bookService;

    @InjectMocks
    Harvester harvester;

    @Test
    void upsertTest() {
        var book = new Book();
        book.setExternalId("externalId");

        var metadata = new Metadata();
        metadata.setDescription("description");

        book.setMetadata(metadata);

        when(metadataGateway.find()).thenReturn(List.of(book));

        harvester.upsert();

        verify(bookService, times(1)).save(List.of(book));
    }
}
