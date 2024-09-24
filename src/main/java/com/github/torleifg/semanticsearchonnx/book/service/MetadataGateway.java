package com.github.torleifg.semanticsearchonnx.book.service;

import com.github.torleifg.semanticsearchonnx.book.domain.Book;

import java.util.List;

public interface MetadataGateway {
    List<Book> find();
}
