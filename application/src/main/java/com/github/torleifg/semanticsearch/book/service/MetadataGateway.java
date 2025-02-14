package com.github.torleifg.semanticsearch.book.service;

import com.github.torleifg.semanticsearch.book.domain.Book;

import java.util.List;

public interface MetadataGateway {
    
    List<Book> find();
}
