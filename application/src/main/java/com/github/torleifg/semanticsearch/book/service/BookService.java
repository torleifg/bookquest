package com.github.torleifg.semanticsearch.book.service;

import com.github.torleifg.semanticsearch.book.domain.Book;
import com.github.torleifg.semanticsearch.book.repository.BookRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class BookService {
    private final MetadataGateway metadataGateway;

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    public BookService(MetadataGateway metadataGateway, BookRepository bookRepository, BookMapper bookMapper) {
        this.metadataGateway = metadataGateway;
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
    }

    @Transactional
    public boolean findAndSave() {
        final List<MetadataDTO> dtos = metadataGateway.find();

        if (dtos.isEmpty()) {
            return false;
        }

        final List<Book> books = dtos.stream()
                .map(bookMapper::toBook)
                .toList();

        bookRepository.save(books);

        return true;
    }

    public List<Book> lastModified() {
        return bookRepository.lastModified(20);
    }

    public List<Book> fullTextSearch(String query) {
        return bookRepository.fullTextSearch(query, 20);
    }

    public List<Book> semanticSearch(String query) {
        return bookRepository.semanticSearch(query, 20);
    }

    public List<Book> semanticSimilarity() {
        return bookRepository.semanticSimilarity(20);
    }
}
