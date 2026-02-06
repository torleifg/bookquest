package com.github.torleifg.bookquest.core.service;

import com.github.torleifg.bookquest.core.repository.LastModifiedRepository;
import com.github.torleifg.bookquest.core.repository.ResumptionToken;
import com.github.torleifg.bookquest.core.repository.ResumptionTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StateServiceTests {

    @Mock
    ResumptionTokenRepository resumptionTokenRepository;

    @Mock
    LastModifiedRepository lastModifiedRepository;

    @InjectMocks
    StateService stateService;

    @Test
    void getTest() {
        var serviceUri = "serviceUri";
        var resumptionToken = new ResumptionToken("token", Instant.now());
        var lastModified = Instant.now();

        when(resumptionTokenRepository.get(serviceUri)).thenReturn(Optional.of(resumptionToken));
        when(lastModifiedRepository.get(serviceUri)).thenReturn(Optional.of(lastModified));

        var state = stateService.get(serviceUri);

        assertTrue(state.resumptionToken().isPresent());
        assertEquals(resumptionToken, state.resumptionToken().get());

        assertTrue(state.lastModified().isPresent());
        assertEquals(lastModified, state.lastModified().get());
    }

    @Test
    void updateTest() {
        var serviceUri = "serviceUri";
        var resumptionToken = "token";
        var lastModified = Instant.now();

        var response = new GatewayResponse("requestUri", List.of(), resumptionToken, lastModified);

        stateService.update(serviceUri, response);

        verify(resumptionTokenRepository).save(serviceUri, resumptionToken);
        verify(lastModifiedRepository).save(serviceUri, lastModified);
    }
}
