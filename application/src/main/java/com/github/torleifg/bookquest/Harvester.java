package com.github.torleifg.bookquest;

import com.github.torleifg.bookquest.core.service.BookService;
import com.github.torleifg.bookquest.core.service.GatewayResponse;
import com.github.torleifg.bookquest.core.service.GatewayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Component
class Harvester {
    private final BookService bookService;

    private final TransactionTemplate transactionTemplate;

    Harvester(BookService bookService, TransactionTemplate transactionTemplate) {
        this.bookService = bookService;
        this.transactionTemplate = transactionTemplate;
    }

    boolean poll(GatewayService gatewayService) {
        final GatewayResponse gatewayResponse = gatewayService.find();

        final int size = gatewayResponse.books().size();

        log.info("Received {} books polling {}", size, gatewayResponse.requestUri());

        if (size == 0) {
            return false;
        }

        transactionTemplate.executeWithoutResult(_ -> {
            bookService.save(gatewayResponse.books());
            gatewayService.updateHarvestState(gatewayResponse);
        });

        return true;
    }
}
