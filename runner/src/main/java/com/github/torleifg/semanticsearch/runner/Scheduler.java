package com.github.torleifg.semanticsearch.runner;

import com.github.torleifg.semanticsearch.book.service.BookService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class Scheduler {
    private final BookService bookService;

    @Value("${harvesting.enabled}")
    private boolean enabled;

    public Scheduler(BookService bookService) {
        this.bookService = bookService;
    }

    @Scheduled(initialDelayString = "${harvesting.initial-delay}", fixedDelayString = "${harvesting.fixed-delay}", timeUnit = TimeUnit.SECONDS)
    public void poll() {
        if (!enabled) {
            return;
        }

        while (true) {
            final boolean continuePoll = bookService.findAndSave();

            if (!continuePoll) {
                break;
            }
        }
    }
}
