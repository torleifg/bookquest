package com.github.torleifg.semanticsearch.book.service;

import com.github.torleifg.semanticsearch.book.domain.Book;
import com.github.torleifg.semanticsearch.book.domain.Metadata;
import com.github.torleifg.semanticsearch.book.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTests {

    @Mock
    MetadataGateway metadataGateway;

    @Mock
    BookRepository bookRepository;

    @Mock
    BookMapper bookMapper;

    @InjectMocks
    BookService bookService;

    @Test
    void findAndSaveBookTest() {
        var dto = new MetadataDTO();
        dto.setExternalId("externalId");
        dto.setDescription("description");

        var book = new Book();
        book.setExternalId("externalId");

        var metadata = new Metadata();
        metadata.setDescription("description");

        book.setMetadata(metadata);

        when(metadataGateway.find()).thenReturn(List.of(dto));
        when(bookMapper.toBook(dto)).thenReturn(book);

        bookService.findAndSave();

        verify(bookRepository, times(1)).save(List.of(book));
    }
}
