package com.github.torleifg.bookquest.gateway.bokbasen;

import com.github.torleifg.bookquest.core.repository.ResumptionToken;
import com.github.torleifg.bookquest.core.repository.ResumptionTokenRepository;
import org.editeur.ns.onix._3_1.reference.ONIXMessage;
import org.editeur.ns.onix._3_1.reference.ObjectFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BokbasenGatewayTests {

    @Mock
    BokbasenClient bokbasenClient;

    @Mock
    BokbasenMapper bokbasenMapper;

    @Mock
    BokbasenProperties.GatewayConfig gatewayConfig;

    @Mock
    ResumptionTokenRepository resumptionTokenRepository;

    @InjectMocks
    BokbasenGateway bokbasenGateway;

    final ObjectFactory objectFactory = new ObjectFactory();

    @Test
    void findAllTest() {
        when(gatewayConfig.getServiceUri()).thenReturn("/harvest");
        when(gatewayConfig.getSubscription()).thenReturn("extended");
        when(gatewayConfig.getPagesize()).thenReturn(100);
        when(gatewayConfig.getAfter()).thenReturn("19700101090000");

        var bokbasenResponse = createResponse();

        when(bokbasenClient.get("/harvest?subscription=extended&pagesize=100&after=19700101090000")).thenReturn(ResponseEntity.ok(bokbasenResponse));

        var gatewayResponse = bokbasenGateway.find();
        assertEquals(0, gatewayResponse.books().size());
    }

    @Test
    void findFromResumptionTokenTest() {
        when(gatewayConfig.getServiceUri()).thenReturn("/harvest");
        when(gatewayConfig.getSubscription()).thenReturn("extended");
        when(gatewayConfig.getPagesize()).thenReturn(100);

        var resumptionToken = "token";
        when(resumptionTokenRepository.get("/harvest")).thenReturn(Optional.of(new ResumptionToken(resumptionToken, Instant.now())));

        var bokbasenResponse = createResponse();

        when(bokbasenClient.get("/harvest?subscription=extended&pagesize=100&next=token")).thenReturn(ResponseEntity.ok(bokbasenResponse));

        var gatewayResponse = bokbasenGateway.find();
        assertEquals(0, gatewayResponse.books().size());
    }

    ONIXMessage createResponse() {
        var response = objectFactory.createONIXMessage();

        var noProduct = objectFactory.createNoProduct();
        response.setNoProduct(noProduct);

        return response;
    }
}
