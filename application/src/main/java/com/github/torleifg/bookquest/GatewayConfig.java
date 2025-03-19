package com.github.torleifg.bookquest;

import com.github.torleifg.bookquest.core.service.GatewayService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
class GatewayConfig {

    @Bean
    List<GatewayService> gateways(List<GatewayService> bibbiGateways, List<GatewayService> bokbasenGateways, List<GatewayService> oaiPmhGateways) {
        final List<GatewayService> gateways = new ArrayList<>();
        gateways.addAll(bibbiGateways);
        gateways.addAll(bokbasenGateways);
        gateways.addAll(oaiPmhGateways);

        return gateways;
    }
}
