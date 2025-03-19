package com.github.torleifg.bookquest;

import com.github.torleifg.bookquest.core.domain.Book;
import com.github.torleifg.bookquest.core.service.BookService;
import com.github.torleifg.bookquest.core.service.GatewayService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
class Harvester {
    private final BookService bookService;

    Harvester(BookService bookService) {
        this.bookService = bookService;
    }

    @Transactional
    boolean poll(GatewayService service) {
        final List<Book> books = service.find();

        if (books.isEmpty()) {
            return false;
        }

        bookService.save(books);

        return true;
    }
}
