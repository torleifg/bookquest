package com.github.torleifg.semanticsearch.gateway.bokbasen;

import com.github.torleifg.semanticsearch.book.repository.ResumptionTokenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.wiremock.spring.EnableWireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnableWireMock
@RestClientTest(BokbasenClient.class)
@TestPropertySource(properties = "gateway.type=bokbasen")
@ContextConfiguration(classes = {BokbasenConfig.class, BokbasenClientTests.BokbasenTestConfig.class})
class BokbasenClientTests {

    @Value("${wiremock.server.baseUrl}")
    String wireMockUrl;

    @Autowired
    BokbasenClient bokbasenClient;

    @MockitoBean
    ResumptionTokenRepository resumptionTokenRepository;

    @Test
    void getTest() {
        stubFor(post("/token").willReturn(okJson("""
                      {
                        "access_token": "MTQ0NjJkZmQ5OTM2NDE1ZTZjNGZmZjI3",
                        "token_type": "Bearer"
                       }
                """)));

        stubFor(get("/onix/v1").willReturn(okXml("""
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <ONIXMessage xmlns="http://ns.editeur.org/onix/3.0/reference" release="3.0">
                    <Header>
                        <Sender>
                            <SenderIdentifier>
                                <SenderIDType>01</SenderIDType>
                                <IDValue>12345</IDValue>
                            </SenderIdentifier>
                        </Sender>
                        <SentDateTime>20240402T071832+0000</SentDateTime>
                    </Header>
                    <NoProduct/>
                </ONIXMessage>
                """)));

        var responseEntity = bokbasenClient.get(wireMockUrl + "/onix/v1");
        var message = responseEntity.getBody();

        assertNotNull(message);
        assertNotNull(message.getNoProduct());
    }

    @TestConfiguration
    static class BokbasenTestConfig {

        @Value("${wiremock.server.baseUrl}")
        String wireMockUrl;

        @Bean
        OAuth2AuthorizedClientService authorizedClientService(ClientRegistrationRepository clientRegistrationRepository) {
            return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
        }

        @Bean
        ClientRegistrationRepository clientRegistrationRepository() {
            return new InMemoryClientRegistrationRepository(ClientRegistration
                    .withRegistrationId("bokbasen")
                    .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                    .clientId("clientId")
                    .clientSecret("clientSecret")
                    .tokenUri(wireMockUrl + "/token")
                    .build());
        }

        @Bean
        BokbasenProperties bokbasenProperties() {
            var bokbasenProperties = new BokbasenProperties();
            bokbasenProperties.setMapper("default");
            bokbasenProperties.setClient("bokbasen");
            bokbasenProperties.setAudience("https://api.bokbasen.io/metadata/");

            return bokbasenProperties;
        }
    }
}
