package com.github.torleifg.bookquest.core.service;

import com.github.torleifg.bookquest.core.domain.Book;
import com.github.torleifg.bookquest.core.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {
    private static final int LIMIT = 50;

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public void save(List<Book> books) {
        bookRepository.save(books);
    }

    public List<Book> latest(String genre) {
        return bookRepository.latest(genre, LIMIT);
    }

    public List<Book> fullTextSearch(String query) {
        return bookRepository.fullTextSearch(query, LIMIT);
    }

    public List<Book> semanticSearch(String query) {
        return bookRepository.semanticSearch(query, LIMIT);
    }

    public List<Book> semanticSimilarity(String isbn) {
        return bookRepository.semanticSimilarity(isbn, LIMIT);
    }

    public List<String> autocomplete(String term) {
        return bookRepository.autocomplete(term, 15);
    }

    public List<Book> hybridSearch(String query) {
        final List<RankedSearchHit> rankedSearchHits = List.of(
                RankedSearchHit.from(bookRepository.fullTextSearch(query, LIMIT), 0.5),
                RankedSearchHit.from(bookRepository.semanticSearch(query, LIMIT), 0.5)
        );

        return new ReciprocalRankFusion(rankedSearchHits)
                .compute()
                .keySet()
                .stream()
                .limit(LIMIT)
                .toList();
    }
}
