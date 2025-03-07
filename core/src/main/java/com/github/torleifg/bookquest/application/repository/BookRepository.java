package com.github.torleifg.bookquest.application.repository;

import com.github.torleifg.bookquest.application.domain.Book;

import java.util.List;

public interface BookRepository {

    void save(List<Book> books);

    List<Book> lastModified(int limit);

    List<Book> fullTextSearch(String query, int limit);

    List<Book> semanticSearch(String query, int limit);

    List<Book> hybridSearch(String query, int limit);

    List<Book> semanticSimilarity(int limit);

    List<Book> semanticSimilarity(String isbn, int limit);
}
