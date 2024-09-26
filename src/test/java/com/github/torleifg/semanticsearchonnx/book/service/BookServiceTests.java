package com.github.torleifg.semanticsearchonnx.book.service;

import com.github.torleifg.semanticsearchonnx.book.domain.Book;
import com.github.torleifg.semanticsearchonnx.book.domain.Metadata;
import com.github.torleifg.semanticsearchonnx.book.repository.BookRepository;
import com.github.torleifg.semanticsearchonnx.book.repository.VectorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTests {

    @Mock
    MetadataGateway metadataGateway;

    @Mock
    BookRepository bookRepository;

    @Mock
    VectorRepository vectorRepository;

    @Mock
    MetadataMapper metadataMapper;

    @InjectMocks
    BookService bookService;

    @Test
    void findAndSaveBookAndVectorTest() {
        var dto = new MetadataDTO();
        dto.setExternalId("externalId");
        dto.setDescription("description");

        var book = new Book();
        book.setExternalId("externalId");

        var metadata = new Metadata();
        metadata.setDescription("description");

        book.setMetadata(metadata);

        when(metadataGateway.find()).thenReturn(List.of(dto));

        when(bookRepository.findVectorStoreIdByExternalId("externalId")).thenReturn(Optional.empty());

        var document = new Document(dto.getDescription());

        when(metadataMapper.toBook(dto)).thenReturn(book);
        when(metadataMapper.toDocument(book)).thenReturn(document);

        bookService.findAndSave();

        verify(vectorRepository, times(1)).save(document);
        verify(bookRepository, times(1)).save(book, UUID.fromString(document.getId()));
    }

    @Test
    void findAndSaveBookAndReplaceVectorTest() {
        var dto = new MetadataDTO();
        dto.setExternalId("externalId");
        dto.setDescription("description");

        var book = new Book();
        book.setExternalId("externalId");

        var metadata = new Metadata();
        metadata.setDescription("description");

        book.setMetadata(metadata);

        when(metadataGateway.find()).thenReturn(List.of(dto));

        var oldVectorId = UUID.randomUUID();

        when(bookRepository.findVectorStoreIdByExternalId("externalId")).thenReturn(Optional.of(oldVectorId));

        var document = new Document(book.getMetadata().getDescription());

        when(metadataMapper.toBook(dto)).thenReturn(book);
        when(metadataMapper.toDocument(book)).thenReturn(document);

        bookService.findAndSave();

        verify(vectorRepository, times(1)).save(document);
        verify(bookRepository, times(1)).save(book, UUID.fromString(document.getId()));
        verify(vectorRepository, times(1)).delete(oldVectorId);
    }
}
