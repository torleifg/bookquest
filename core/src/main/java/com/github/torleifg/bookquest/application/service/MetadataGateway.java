package com.github.torleifg.bookquest.application.service;

import com.github.torleifg.bookquest.application.domain.Book;

import java.util.List;

public interface MetadataGateway {
    List<Book> find();
}
