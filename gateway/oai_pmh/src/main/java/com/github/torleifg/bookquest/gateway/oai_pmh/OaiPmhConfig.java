package com.github.torleifg.bookquest.gateway.oai_pmh;

import com.github.torleifg.bookquest.core.repository.LastModifiedRepository;
import com.github.torleifg.bookquest.core.repository.ResumptionTokenRepository;
import com.github.torleifg.bookquest.core.service.MetadataGateway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@ConditionalOnProperty(prefix = "harvesting", name = "gateway", havingValue = "oai-pmh")
class OaiPmhConfig {

    @Bean
    MetadataGateway metadataGateway(OaiPmhClient oaiPmhClient, OaiPmhMapper oaiPmhMapper, OaiPmhProperties oaiPmhProperties, ResumptionTokenRepository resumptionTokenRepository, LastModifiedRepository lastModifiedRepository) {
        return new OaiPmhGateway(oaiPmhClient, oaiPmhMapper, oaiPmhProperties, resumptionTokenRepository, lastModifiedRepository);
    }

    @Bean
    OaiPmhMapper oaiPmhMapper(OaiPmhProperties oaiPmhProperties) {
        final String mapper = oaiPmhProperties.getMapper();

        /*
        Mapper factory
         */

        return new OaiPmhDefaultMapper();
    }

    @Bean
    OaiPmhClient oaiPmhClient(RestClient.Builder builder) {
        final RestClient restClient = builder
                .requestFactory(new JdkClientHttpRequestFactory())
                .build();

        return new OaiPmhClient(restClient);
    }
}
