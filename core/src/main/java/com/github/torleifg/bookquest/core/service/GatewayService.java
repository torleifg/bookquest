package com.github.torleifg.bookquest.core.service;

import com.github.torleifg.bookquest.core.domain.Book;

import java.util.List;

public interface GatewayService {

    List<Book> find();
}
