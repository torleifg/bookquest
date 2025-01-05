package com.github.torleifg.semanticsearch.gateway.bibbi;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.torleifg.semanticsearch.book.repository.LastModifiedRepository;
import com.github.torleifg.semanticsearch.book.repository.ResumptionTokenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RestClientTest(BibbiClient.class)
@EnableWireMock({@ConfigureWireMock(port = 8888)})
@TestPropertySource(properties = "gateway.type=bibbi")
@ContextConfiguration(classes = {BibbiConfig.class, BibbiClientTests.BibbiTestConfig.class})
class BibbiClientTests {

    @InjectWireMock
    WireMockServer wm;

    @Autowired
    BibbiClient bibbiClient;

    @MockitoBean
    ResumptionTokenRepository resumptionTokenRepository;

    @MockitoBean
    LastModifiedRepository lastModifiedRepository;

    @Test
    void getTest() {
        wm.stubFor(get("/v1/publications/harvest").willReturn(okJson("""
                {
                  "publications": [],
                  "total": 0
                }
                """)));

        var response = bibbiClient.get(wm.baseUrl() + "/v1/publications/harvest");

        assertNotNull(response.getPublications());
        assertTrue(response.getPublications().isEmpty());
    }

    @TestConfiguration
    static class BibbiTestConfig {

        @Bean
        BibbiProperties bibbiProperties() {
            var bibbiProperties = new BibbiProperties();
            bibbiProperties.setMapper("default");

            return bibbiProperties;
        }
    }
}
