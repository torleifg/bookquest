package com.github.torleifg.bookquest;

import com.github.torleifg.bookquest.core.service.BookService;
import com.github.torleifg.bookquest.core.service.GatewayResponse;
import com.github.torleifg.bookquest.core.service.GatewayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
class Harvester {
    private final List<GatewayService> gateways;

    private final BookService bookService;
    private final TransactionTemplate transactionTemplate;

    private final boolean enabled;

    Harvester(List<GatewayService> gateways, BookService bookService, TransactionTemplate transactionTemplate, @Value("${scheduler.enabled}") boolean enabled) {
        this.gateways = gateways;
        this.bookService = bookService;
        this.transactionTemplate = transactionTemplate;
        this.enabled = enabled;
    }

    @Scheduled(initialDelayString = "${scheduler.initial-delay}", fixedDelayString = "${scheduler.fixed-delay}", timeUnit = TimeUnit.SECONDS)
    void run() {
        if (!enabled) {
            log.warn("Scheduler is disabled.");
            return;
        }

        if (gateways.isEmpty()) {
            log.warn("No gateways available.");
            return;
        }

        final Queue<GatewayService> queue = new ArrayDeque<>(gateways);

        while (!queue.isEmpty()) {
            final GatewayService gateway = queue.poll();
            final String name = gateway.getClass().getSimpleName();

            log.info("Polling {}...", name);

            try {
                final GatewayResponse response = gateway.find();

                if (response.books().isEmpty()) {
                    log.info("Finished polling {}", name);
                    continue;
                }

                log.info("Received {} books polling {}", response.books().size(), response.requestUri());

                transactionTemplate.executeWithoutResult(_ -> {
                    bookService.save(response.books());
                    gateway.updateHarvestState(response);
                });

                queue.add(gateway);

            } catch (Exception e) {
                log.error("Error while processing gateway {}", name, e);
            }
        }
    }
}
