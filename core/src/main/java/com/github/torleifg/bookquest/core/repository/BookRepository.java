package com.github.torleifg.bookquest.core.repository;

import com.github.torleifg.bookquest.core.domain.Book;
import com.github.torleifg.bookquest.core.domain.Suggestion;

import java.util.List;

public interface BookRepository {

    void save(List<Book> books);

    List<Book> latest(String genre, int limit);

    List<Book> fullTextSearch(String query, int limit);

    List<Book> semanticSearch(String query, int limit);

    List<Book> semanticSimilarity(int limit);

    List<Book> semanticSimilarity(String isbn, int limit);

    List<Suggestion> autocomplete(String term, int limit);
}
