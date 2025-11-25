package com.github.torleifg.bookquest;

import com.github.torleifg.bookquest.core.service.BookService;
import com.github.torleifg.bookquest.core.service.GatewayResponse;
import com.github.torleifg.bookquest.core.service.GatewayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
class Harvester {
    private final BookService bookService;

    Harvester(BookService bookService) {
        this.bookService = bookService;
    }

    boolean poll(GatewayService gatewayService) {
        final GatewayResponse gatewayResponse = gatewayService.find();

        final int size = gatewayResponse.books().size();

        log.info("Received {} books polling {}", size, gatewayResponse.requestUri());

        if (size == 0) {
            return false;
        }

        saveAll(gatewayResponse, gatewayService);

        return true;
    }

    @Transactional
    private void saveAll(GatewayResponse gatewayResponse, GatewayService gatewayService) {
        bookService.save(gatewayResponse.books());

        gatewayService.updateHarvestState(gatewayResponse);
    }
}
