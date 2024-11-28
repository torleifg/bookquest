package com.github.torleifg.semanticsearchonnx.book.service;

import com.github.torleifg.semanticsearchonnx.book.domain.Book;
import com.github.torleifg.semanticsearchonnx.book.repository.BookRepository;
import com.github.torleifg.semanticsearchonnx.book.repository.VectorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
public class BookService {
    private final MetadataGateway metadataGateway;

    private final BookRepository bookRepository;
    private final VectorRepository vectorRepository;

    private final MetadataMapper metadataMapper;

    public BookService(MetadataGateway metadataGateway, BookRepository bookRepository, VectorRepository vectorRepository, MetadataMapper metadataMapper) {
        this.metadataGateway = metadataGateway;
        this.bookRepository = bookRepository;
        this.vectorRepository = vectorRepository;
        this.metadataMapper = metadataMapper;
    }

    @Transactional
    public boolean findAndSave() {
        final List<MetadataDTO> dtos = metadataGateway.find();

        if (dtos.isEmpty()) {
            return false;
        }

        dtos.stream()
                .map(metadataMapper::toBook)
                .forEach(this::save);

        return true;
    }

    private void save(Book book) {
        final String externalId = book.getExternalId();

        if (book.isDeleted()) {
            bookRepository.findByExternalId(externalId).ifPresent(existingBook -> {
                existingBook.setDeleted(true);

                bookRepository.save(existingBook);
            });

            return;
        }

        final Optional<UUID> existingVector = bookRepository.findVectorStoreIdByExternalId(externalId);

        final Document document = metadataMapper.toDocument(book);
        vectorRepository.save(document);

        final UUID newVector = UUID.fromString(document.getId());
        bookRepository.save(book, newVector);

        existingVector.ifPresent(vectorRepository::delete);
    }

    public List<Map<String, Object>> semanticSearch(String query) {
        final List<UUID> ids = vectorRepository.query(query, 20).stream()
                .map(Document::getId)
                .map(UUID::fromString)
                .toList();

        final List<Book> books = bookRepository.findByVectorStoreIdsIn(ids);

        return asMap(books);
    }

    public List<Map<String, Object>> passage() {
        final List<UUID> ids = vectorRepository.passage(20).stream()
                .map(Document::getId)
                .map(UUID::fromString)
                .toList();

        final List<Book> books = bookRepository.findByVectorStoreIdsIn(ids);

        return asMap(books);
    }

    public List<Map<String, Object>> fullTextSearch(String query) {
        final List<Book> books = bookRepository.query(query, 20);

        return asMap(books);
    }

    private List<Map<String, Object>> asMap(List<Book> books) {
        final List<Map<String, Object>> metadata = new ArrayList<>();

        for (final Book book : books) {
            metadata.add(metadataMapper.toMap(book));
        }

        return metadata;
    }
}
