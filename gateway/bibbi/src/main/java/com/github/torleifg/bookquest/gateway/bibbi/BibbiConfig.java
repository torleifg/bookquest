package com.github.torleifg.bookquest.gateway.bibbi;

import com.github.torleifg.bookquest.core.repository.LastModifiedRepository;
import com.github.torleifg.bookquest.core.repository.ResumptionTokenRepository;
import com.github.torleifg.bookquest.core.service.GatewayService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.List;

@Configuration
class BibbiConfig {

    @Bean
    List<GatewayService> bibbiGateways(BibbiProperties bibbiProperties, BibbiClient bibbiClient, ResumptionTokenRepository resumptionTokenRepository, LastModifiedRepository lastModifiedRepository) {
        return bibbiProperties.getGateways().stream()
                .filter(BibbiProperties.GatewayConfig::isEnabled)
                .map(config -> createGateway(config, bibbiClient, resumptionTokenRepository, lastModifiedRepository))
                .map(GatewayService.class::cast)
                .toList();
    }

    private BibbiGateway createGateway(BibbiProperties.GatewayConfig gatewayConfig, BibbiClient bibbiClient, ResumptionTokenRepository resumptionTokenRepository, LastModifiedRepository lastModifiedRepository) {
        final String mapper = gatewayConfig.getMapper();

        /*
        Mapper factory
         */

        final BibbiMapper bibbiMapper = new BibbiDefaultMapper();

        return new BibbiGateway(gatewayConfig, bibbiClient, bibbiMapper, resumptionTokenRepository, lastModifiedRepository);
    }

    @Bean
    BibbiClient bibbiClient(RestClient.Builder builder) {
        final RestClient restClient = builder
                .requestFactory(new JdkClientHttpRequestFactory())
                .build();

        return new BibbiClient(restClient);
    }
}
