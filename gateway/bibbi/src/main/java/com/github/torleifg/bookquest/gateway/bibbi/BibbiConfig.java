package com.github.torleifg.bookquest.gateway.bibbi;

import com.github.torleifg.bookquest.core.repository.LastModifiedRepository;
import com.github.torleifg.bookquest.core.repository.ResumptionTokenRepository;
import com.github.torleifg.bookquest.core.service.MetadataGateway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@ConditionalOnProperty(prefix = "harvesting", name = "gateway", havingValue = "bibbi")
class BibbiConfig {

    @Bean
    MetadataGateway metadataGateway(BibbiClient bibbiClient, BibbiMapper bibbiMapper, BibbiProperties bibbiProperties, ResumptionTokenRepository resumptionTokenRepository, LastModifiedRepository lastModifiedRepository) {
        return new BibbiGateway(bibbiClient, bibbiMapper, bibbiProperties, resumptionTokenRepository, lastModifiedRepository);
    }

    @Bean
    BibbiMapper bibbiMapper(BibbiProperties bibbiProperties) {
        final String mapper = bibbiProperties.getMapper();

        /*
        Mapper factory
         */

        return new BibbiDefaultMapper();
    }

    @Bean
    BibbiClient bibbiClient(RestClient.Builder builder) {
        final RestClient restClient = builder
                .requestFactory(new JdkClientHttpRequestFactory())
                .build();

        return new BibbiClient(restClient);
    }
}
