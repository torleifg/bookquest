package com.github.torleifg.bookquest;

import com.github.torleifg.bookquest.core.domain.Book;
import com.github.torleifg.bookquest.core.service.BookService;
import com.github.torleifg.bookquest.core.service.GatewayResponse;
import com.github.torleifg.bookquest.core.service.GatewayService;
import com.github.torleifg.bookquest.core.service.StateService;
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
class HarvesterTests {

    @Mock
    GatewayService firstGateway;

    @Mock
    GatewayService secondGateway;

    @Mock
    BookService bookService;

    @Mock
    StateService stateService;

    @Mock
    TransactionTemplate transactionTemplate;

    Harvester harvester;

    @BeforeEach
    void setUp() {
        harvester = new Harvester(List.of(firstGateway, secondGateway), bookService, stateService, transactionTemplate, 60);
    }

    @Test
    void runTest() {
        var response = new GatewayResponse(null, List.of(new Book()), null, null);
        when(firstGateway.find(any())).thenReturn(response);

        var emptyResponse = new GatewayResponse(null, List.of(), null, null);
        when(secondGateway.find(any())).thenReturn(emptyResponse);

        doAnswer(invocation -> {
            Consumer<TransactionStatus> callback = invocation.getArgument(0);
            callback.accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());

        harvester.run();

        var inOrder = inOrder(firstGateway, secondGateway);
        inOrder.verify(firstGateway).find(any());
        inOrder.verify(secondGateway).find(any());

        verify(firstGateway, times(1)).find(any());
        verify(secondGateway, times(1)).find(any());

        verify(bookService, times(1)).save(anyList());
        verify(stateService, times(1)).update(any(), any());
    }

    @Test
    void runBackoffEmptyResponseTest() {
        var emptyResponse = new GatewayResponse(null, List.of(), null, null);

        when(firstGateway.find(any())).thenReturn(emptyResponse);
        when(secondGateway.find(any())).thenReturn(emptyResponse);

        harvester.run();
        harvester.run();

        verify(firstGateway, times(1)).find(any());
        verify(secondGateway, times(1)).find(any());

        verifyNoInteractions(bookService);
    }

    @Test
    void runBackoffExceptionTest() {
        when(firstGateway.find(any())).thenThrow(new RuntimeException("..."));
        when(secondGateway.find(any())).thenThrow(new RuntimeException("..."));

        harvester.run();
        harvester.run();

        verify(firstGateway, times(1)).find(any());
        verify(secondGateway, times(1)).find(any());
        verifyNoInteractions(bookService);
    }

    @Test
    void runResumeAfterBackoffTest() {
        var harvester = new Harvester(List.of(firstGateway), bookService, stateService, transactionTemplate, 0);

        var emptyResponse = new GatewayResponse(null, List.of(), null, null);
        when(firstGateway.find(any())).thenReturn(emptyResponse);

        harvester.run();
        harvester.run();

        verify(firstGateway, times(2)).find(any());
        verifyNoInteractions(bookService);
    }
}
