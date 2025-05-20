package com.github.torleifg.bookquest.core.repository;

import com.github.torleifg.bookquest.core.domain.Book;

import java.util.List;

public interface BookRepository {

    void save(List<Book> books);

    List<Book> lastModified(int limit);

    List<Book> fullTextSearch(String query, int limit);

    List<Book> semanticSearch(String query, int limit);

    List<Book> hybridSearch(String query, int limit);

    List<Book> semanticSimilarity(int limit);

    List<Book> semanticSimilarity(String isbn, int limit);

    List<String> autocomplete(String term, int limit);
}
