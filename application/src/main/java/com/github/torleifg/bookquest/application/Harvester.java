package com.github.torleifg.bookquest.application;

import com.github.torleifg.bookquest.application.domain.Book;
import com.github.torleifg.bookquest.application.service.BookService;
import com.github.torleifg.bookquest.application.service.MetadataGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
class Harvester {
    private final MetadataGateway metadataGateway;
    private final BookService bookService;

    @Value("${harvesting.enabled}")
    private boolean enabled;

    Harvester(MetadataGateway metadataGateway, BookService bookService) {
        this.metadataGateway = metadataGateway;
        this.bookService = bookService;
    }

    @Scheduled(initialDelayString = "${harvesting.initial-delay}", fixedDelayString = "${harvesting.fixed-delay}", timeUnit = TimeUnit.SECONDS)
    void poll() {
        if (!enabled) {
            return;
        }

        while (true) {
            final boolean continuePoll = upsert();

            if (!continuePoll) {
                break;
            }
        }
    }

    boolean upsert() {
        final List<Book> books = metadataGateway.find();

        if (books.isEmpty()) {
            return false;
        }

        bookService.save(books);

        return true;
    }
}
