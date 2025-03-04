package com.github.torleifg.bookquest.application.service;

import com.github.torleifg.bookquest.application.domain.Book;
import com.github.torleifg.bookquest.application.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> lastModified() {
        return bookRepository.lastModified(20);
    }

    public List<Book> hybridSearch(String query) {
        return bookRepository.hybridSearch(query, 20);
    }

    public List<Book> semanticSimilarity() {
        return bookRepository.semanticSimilarity(20);
    }
}
