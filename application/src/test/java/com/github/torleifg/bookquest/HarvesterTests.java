package com.github.torleifg.bookquest;

import com.github.torleifg.bookquest.core.domain.Book;
import com.github.torleifg.bookquest.core.service.BookService;
import com.github.torleifg.bookquest.core.service.GatewayResponse;
import com.github.torleifg.bookquest.core.service.GatewayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.function.Consumer;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HarvesterTests {

    @Mock
    GatewayService firstGateway;

    @Mock
    GatewayService secondGateway;

    @Mock
    BookService bookService;

    @Mock
    TransactionTemplate transactionTemplate;

    Harvester harvester;

    @BeforeEach
    void setUp() {
        harvester = new Harvester(List.of(firstGateway, secondGateway), bookService, transactionTemplate, true);
    }

    @Test
    void runTest() {
        var response = new GatewayResponse(null, List.of(new Book()), null, null);
        var emptyResponse = new GatewayResponse(null, List.of(), null, null);

        when(firstGateway.find()).thenReturn(response, emptyResponse);
        when(secondGateway.find()).thenReturn(emptyResponse);

        doAnswer(invocation -> {
            Consumer<TransactionStatus> callback = invocation.getArgument(0);
            callback.accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());

        harvester.run();

        var inOrder = inOrder(firstGateway, secondGateway);
        inOrder.verify(firstGateway).find();
        inOrder.verify(secondGateway).find();
        inOrder.verify(firstGateway).find();

        verify(firstGateway, times(2)).find();
        verify(secondGateway, times(1)).find();

        verify(bookService, times(1)).save(anyList());
        verify(firstGateway, times(1)).updateHarvestState(response);
        verify(secondGateway, never()).updateHarvestState(any());
    }
}
