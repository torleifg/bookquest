package com.github.torleifg.bookquest.application.service;

import com.github.torleifg.bookquest.application.domain.Book;
import com.github.torleifg.bookquest.application.domain.Metadata;
import com.github.torleifg.bookquest.application.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetadataServiceTests {

    @Mock
    MetadataGateway metadataGateway;

    @Mock
    BookRepository bookRepository;

    @InjectMocks
    MetadataService metadataService;

    @Test
    void findAndSaveBookTest() {
        var book = new Book();
        book.setExternalId("externalId");

        var metadata = new Metadata();
        metadata.setDescription("description");

        book.setMetadata(metadata);

        when(metadataGateway.find()).thenReturn(List.of(book));

        metadataService.findAndSave();

        verify(bookRepository, times(1)).save(List.of(book));
    }
}
