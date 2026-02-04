package com.github.torleifg.bookquest;

import com.github.torleifg.bookquest.core.service.BookService;
import com.github.torleifg.bookquest.core.service.GatewayResponse;
import com.github.torleifg.bookquest.core.service.GatewayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.ClassUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
class Harvester {
    private final List<GatewayService> gateways;

    private final BookService bookService;
    private final TransactionTemplate transactionTemplate;
    private final int backoffSeconds;

    private final Map<GatewayService, Instant> backoffRegistry = new ConcurrentHashMap<>();

    Harvester(List<GatewayService> gateways, BookService bookService, TransactionTemplate transactionTemplate, int backoffSeconds) {
        this.gateways = gateways;
        this.bookService = bookService;
        this.transactionTemplate = transactionTemplate;
        this.backoffSeconds = backoffSeconds;
    }

    @Scheduled(initialDelayString = "${scheduler.initial-delay-millis}", fixedDelayString = "${scheduler.fixed-delay-millis}", timeUnit = TimeUnit.MILLISECONDS)
    void run() {
        final Instant now = Instant.now();

        for (final GatewayService gateway : gateways) {
            final String name = ClassUtils.getUserClass(gateway).getSimpleName();

            final Instant backoff = backoffRegistry.get(gateway);

            if (backoff != null) {
                if (now.isBefore(backoff)) {
                    log.debug("Skipping polling {}. Backoff until {}", name, backoff);
                    continue;
                }

                backoffRegistry.remove(gateway);
            }

            try {
                log.info("Polling {}...", name);

                final GatewayResponse response = gateway.find();

                if (response.books().isEmpty()) {
                    log.info("No books found polling {}. Backing off for {} seconds", name, backoffSeconds);
                    backoffRegistry.put(gateway, now.plusSeconds(backoffSeconds));
                    continue;
                }

                log.info("Received {} books polling {}", response.books().size(), response.requestUri());

                transactionTemplate.executeWithoutResult(_ -> {
                    bookService.save(response.books());
                    gateway.updateHarvestState(response);
                });

            } catch (Exception e) {
                log.error("Error while polling {}. Backing off for {} seconds.", name, backoffSeconds, e);

                backoffRegistry.put(gateway, now.plusSeconds(backoffSeconds));
            }
        }
    }
}
