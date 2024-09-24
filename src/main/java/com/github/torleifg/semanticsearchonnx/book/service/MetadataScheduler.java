package com.github.torleifg.semanticsearchonnx.book.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class MetadataScheduler {
    private final BookService bookService;

    @Value("${scheduler.enabled}")
    private boolean enabled;

    public MetadataScheduler(BookService bookService) {
        this.bookService = bookService;
    }

    @Scheduled(initialDelayString = "${metadata.initial-delay}", fixedDelayString = "${metadata.fixed-delay}", timeUnit = TimeUnit.SECONDS)
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
