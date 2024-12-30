package com.github.torleifg.semanticsearch.gateway.bibbi;

import com.github.torleifg.semanticsearch.book.repository.LastModifiedRepository;
import com.github.torleifg.semanticsearch.book.repository.ResumptionTokenRepository;
import com.github.torleifg.semanticsearch.book.service.MetadataGateway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@ConditionalOnProperty(prefix = "gateway", name = "type", havingValue = "bibbi")
class BibbiConfig {

    @Bean
    MetadataGateway metadataGateway(BibbiClient bibbiClient, BibbiMapper bibbiMapper, BibbiProperties bibbiProperties, ResumptionTokenRepository resumptionTokenRepository, LastModifiedRepository lastModifiedRepository) {
        return new BibbiGateway(bibbiClient, bibbiMapper, bibbiProperties, resumptionTokenRepository, lastModifiedRepository);
    }

    @Bean
    BibbiMapper bibbiMapper(BibbiProperties bibbiProperties) {
        return new BibbiDefaultMapper();
    }

    @Bean
    BibbiClient bibbiClient(RestClient restClient) {
        return new BibbiClient(restClient);
    }

    @Bean
    RestClient restClient(RestClient.Builder builder) {
        return builder
                .requestFactory(new JdkClientHttpRequestFactory())
                .build();
    }
}
