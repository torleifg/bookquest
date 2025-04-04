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

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HarvesterTests {

    @Mock
    GatewayService gateway;

    @Mock
    BookService bookService;

    @InjectMocks
    Harvester harvester;

    @Test
    void pollTest() {
        var book = new Book();
        book.setExternalId("externalId");

        var metadata = new Metadata();
        metadata.setDescription("description");

        book.setMetadata(metadata);

        when(gateway.find()).thenReturn(new GatewayResponse("requestUri", List.of(book)));

        harvester.poll(gateway);

        verify(bookService, times(1)).save(List.of(book));
    }
}
