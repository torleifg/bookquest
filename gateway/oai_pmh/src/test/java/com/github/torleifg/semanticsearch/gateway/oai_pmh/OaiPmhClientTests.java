package com.github.torleifg.semanticsearch.gateway.oai_pmh;

import com.github.torleifg.semanticsearch.book.repository.LastModifiedRepository;
import com.github.torleifg.semanticsearch.book.repository.ResumptionTokenRepository;
import org.junit.jupiter.api.Test;
import org.openarchives.oai._2.OAIPMHerrorcodeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.wiremock.spring.EnableWireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@EnableWireMock
@RestClientTest(OaiPmhClient.class)
@TestPropertySource(properties = "gateway.type=oai-pmh")
@ContextConfiguration(classes = {OaiPmhConfig.class, OaiPmhClientTests.OaiPmhTestConfig.class})
class OaiPmhClientTests {

    @Value("${wiremock.server.baseUrl}")
    String wireMockUrl;

    @Autowired
    OaiPmhClient oaiPmhClient;

    @MockitoBean
    ResumptionTokenRepository resumptionTokenRepository;

    @MockitoBean
    LastModifiedRepository lastModifiedRepository;

    @Test
    void getTest() {
        stubFor(get("/mlnb").willReturn(okXml("""
                <?xml version="1.0" encoding="UTF-8"?>
                <OAI-PMH xmlns="http://www.openarchives.org/OAI/2.0/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/ http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd">
                    <responseDate>2025-01-01T12:00:00Z</responseDate>
                    <request from="2030-01-01" metadataPrefix="marc21" verb="ListRecords">https://oai.aja.bs.no/mlnb</request>
                    <error code="noRecordsMatch">no records found</error>
                </OAI-PMH>
                """)));

        var response = oaiPmhClient.get(wireMockUrl + "/mlnb");

        assertEquals(1, response.getError().size());
        assertEquals(OAIPMHerrorcodeType.NO_RECORDS_MATCH, response.getError().getFirst().getCode());
    }

    @TestConfiguration
    static class OaiPmhTestConfig {

        @Bean
        OaiPmhProperties oaiPmhProperties() {
            var oaiPmhProperties = new OaiPmhProperties();
            oaiPmhProperties.setMapper("default");

            return oaiPmhProperties;
        }
    }
}
