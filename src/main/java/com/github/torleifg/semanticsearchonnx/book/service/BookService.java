package com.github.torleifg.semanticsearchonnx.book.service;

import com.github.torleifg.semanticsearchonnx.book.domain.Book;
import com.github.torleifg.semanticsearchonnx.book.repository.BookRepository;
import com.github.torleifg.semanticsearchonnx.book.repository.VectorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class BookService {
    private final MetadataGateway metadataGateway;

    private final BookRepository bookRepository;
    private final VectorRepository vectorRepository;

    private final DocumentMapper documentMapper;

    public BookService(MetadataGateway metadataGateway, BookRepository bookRepository, VectorRepository vectorRepository, DocumentMapper documentMapper) {
        this.metadataGateway = metadataGateway;
        this.bookRepository = bookRepository;
        this.vectorRepository = vectorRepository;
        this.documentMapper = documentMapper;
    }

    @Transactional
    public boolean findAndSave() {
        final List<Book> books = metadataGateway.find();

        if (books.isEmpty()) {
            return false;
        }

        books.forEach(this::save);

        return true;
    }

    private void save(Book book) {
        final String code = book.getCode();

        if (book.isDeleted()) {
            bookRepository.findByCode(code).ifPresent(existingBook -> {
                existingBook.setDeleted(true);

                log.trace("Setting book as deleted for: {}", code);

                bookRepository.save(existingBook);
            });

            return;
        }

        final Optional<UUID> existingVector = bookRepository.findVectorStoreIdByCode(code);

        final Document document = documentMapper.from(book);
        final UUID newVector = UUID.fromString(document.getId());

        vectorRepository.save(document);
        bookRepository.save(book, newVector);

        existingVector.ifPresent(vector -> {
            log.trace("Replacing vector for: {}", code);

            vectorRepository.delete(vector);
        });
    }

    public List<Document> search(String query) {
        return vectorRepository.query(query, 10);
    }

    public List<Document> passage() {
        return vectorRepository.passage(10);
    }
}
