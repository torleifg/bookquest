package com.github.torleifg.bookquest.core.service;

import com.github.torleifg.bookquest.core.domain.Book;
import com.github.torleifg.bookquest.core.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public void save(List<Book> books) {
        bookRepository.save(books);
    }

    public List<Book> lastModified() {
        return bookRepository.lastModified(50);
    }

    public List<Book> fullTextSearch(String query) {
        return bookRepository.fullTextSearch(query, 25);
    }

    public List<Book> semanticSearch(String query) {
        return bookRepository.semanticSearch(query, 25);
    }

    public List<Book> hybridSearch(String query) {
        return bookRepository.hybridSearch(query, 25);
    }

    public List<Book> semanticSimilarity(String isbn) {
        return bookRepository.semanticSimilarity(isbn, 25);
    }

    public List<String> autocomplete(String term) {
        return bookRepository.autocomplete(term, 10);
    }
}
