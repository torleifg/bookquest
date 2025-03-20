package com.github.torleifg.bookquest;

import com.github.torleifg.bookquest.core.service.GatewayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SchedulerTests {

    @Mock
    Harvester harvester;

    Scheduler scheduler;

    @Mock
    private GatewayService firstGateway;

    @Mock
    private GatewayService secondGateway;

    @BeforeEach
    void setUp() {
        var gateways = new ArrayList<>(List.of(firstGateway, secondGateway));

        scheduler = new Scheduler(gateways, harvester, true);
    }

    @Test
    void stopPollingTest() {
        when(harvester.poll(firstGateway)).thenReturn(false);
        when(harvester.poll(secondGateway)).thenReturn(false);

        scheduler.run();

        verify(harvester, times(1)).poll(firstGateway);
        verify(harvester, times(1)).poll(secondGateway);
    }

    @Test
    void continuePollingTest() {
        when(harvester.poll(firstGateway)).thenReturn(true, false);
        when(harvester.poll(secondGateway)).thenReturn(true, false);

        scheduler.run();

        verify(harvester, times(2)).poll(firstGateway);
        verify(harvester, times(2)).poll(secondGateway);
    }
}
