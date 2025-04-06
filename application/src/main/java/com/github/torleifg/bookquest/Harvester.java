package com.github.torleifg.bookquest;

import com.github.torleifg.bookquest.core.domain.Book;
import com.github.torleifg.bookquest.core.service.BookService;
import com.github.torleifg.bookquest.core.service.GatewayResponse;
import com.github.torleifg.bookquest.core.service.GatewayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
class Harvester {
    private final BookService bookService;

    Harvester(BookService bookService) {
        this.bookService = bookService;
    }

    @Transactional
    boolean poll(GatewayService gatewayService) {
        final GatewayResponse response = gatewayService.find();

        final List<Book> books = response.books();

        log.info("Received {} books polling {}", books.size(), response.requestUri());

        if (books.isEmpty()) {
            return false;
        }

        bookService.save(books);

        return true;
    }
}
