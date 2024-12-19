package com.github.torleifg.semanticsearch.book.repository;

import com.github.torleifg.semanticsearch.book.domain.Book;

import java.util.List;

public interface BookRepository {

    void save(List<Book> books);

    List<Book> fullTextSearch(String query, int limit);

    List<Book> semanticSearch(String query, int limit);

    List<Book> semanticSimilarity();
}
