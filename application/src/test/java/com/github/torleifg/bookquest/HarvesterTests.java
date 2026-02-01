package com.github.torleifg.bookquest;

import com.github.torleifg.bookquest.core.domain.Book;
import com.github.torleifg.bookquest.core.domain.Metadata;
import com.github.torleifg.bookquest.core.service.BookService;
import com.github.torleifg.bookquest.core.service.GatewayResponse;
import com.github.torleifg.bookquest.core.service.GatewayService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HarvesterTests {

    @Mock
    GatewayService gateway;

    @Mock
    BookService bookService;

    @Mock
    TransactionTemplate transactionTemplate;

    @InjectMocks
    Harvester harvester;

    @Test
    void pollTest() {
        var book = new Book();
        book.setExternalId("externalId");

        var metadata = new Metadata();
        metadata.setDescription("description");

        book.setMetadata(metadata);

        var response = new GatewayResponse("requestUri", List.of(book), null, null);

        when(gateway.find()).thenReturn(response);

        doAnswer(invocation -> {
            Consumer<TransactionStatus> callback = invocation.getArgument(0);
            callback.accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());

        assertTrue(harvester.poll(gateway));

        var inOrder = inOrder(bookService, gateway);
        inOrder.verify(bookService).save(List.of(book));
        inOrder.verify(gateway).updateHarvestState(response);
    }
}
