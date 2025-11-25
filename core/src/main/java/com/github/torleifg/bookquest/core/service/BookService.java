package com.github.torleifg.bookquest.core.service;

import com.github.torleifg.bookquest.core.domain.Book;
import com.github.torleifg.bookquest.core.domain.Suggestion;
import com.github.torleifg.bookquest.core.repository.BookRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    @Value("${default-limit}")
    private int limit;

    @Value("${reciprocal-rank-fusion.semantic-search-weight}")
    private double semanticSearchWeight;

    @Value("${reciprocal-rank-fusion.full-text-search-weight}")
    private double fullTextSearchWeight;

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public void save(List<Book> books) {
        bookRepository.save(books);
    }

    public List<Book> latest(String genre) {
        return bookRepository.latest(genre, limit);
    }

    public List<Book> fullTextSearch(String query) {
        return bookRepository.fullTextSearch(query, limit);
    }

    public List<Book> semanticSearch(String query) {
        return bookRepository.semanticSearch(query, limit);
    }

    public List<Book> semanticSimilarity(String isbn) {
        return bookRepository.semanticSimilarity(isbn, limit);
    }

    public List<Suggestion> autocomplete(String term) {
        return bookRepository.autocomplete(term, limit / 2);
    }

    public List<Book> hybridSearch(String query) {
        final List<RankedSearchHit> rankedSearchHits = List.of(
                RankedSearchHit.from(bookRepository.fullTextSearch(query, limit), fullTextSearchWeight),
                RankedSearchHit.from(bookRepository.semanticSearch(query, limit), semanticSearchWeight)
        );

        return new ReciprocalRankFusion(rankedSearchHits)
                .compute()
                .keySet()
                .stream()
                .limit(limit)
                .toList();
    }
}
