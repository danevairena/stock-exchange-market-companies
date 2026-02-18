package com.example.stockcompanies.client;

import com.example.stockcompanies.dto.FinnhubCompanyProfileResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// check that an HTTP call is made, URL is correct, JSON is parsed correctly
// without actually calling Finnhub
@SpringBootTest(
        classes = FinnhubFeignClientWireMockIT.TestApp.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
class FinnhubFeignClientWireMockIT {

    // starts fake HTTP server
    // dynamicPort() WireMock chooses a free port
    static WireMockServer wm;

    @Autowired
    FinnhubFeignClient client;

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class,
            SqlInitializationAutoConfiguration.class
    })
    @EnableFeignClients(clients = FinnhubFeignClient.class)
    static class TestApp {
    }

    @BeforeAll
    static void startServer() {
        wm = new WireMockServer(wireMockConfig().dynamicPort());
        wm.start();
        configureFor("localhost", wm.port());
    }

    @AfterAll
    static void stopServer() {
        if (wm != null) {
            wm.stop();
        }
    }

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("finnhub.base-url", () -> "http://localhost:" + wm.port());
        registry.add("finnhub.api-key", () -> "test-api-key");
    }

    @Test
    void getCompanyProfile2_shouldReturnParsedResponse() {
        String symbol = "AAPL";
        String apiKey = "test-api-key";

        // when calling GET /stock/profile2"
        // query params check
        // WireMock will return response only if these params are correct
        stubFor(get(urlPathEqualTo("/stock/profile2"))
                .withQueryParam("symbol", equalTo(symbol))
                .withQueryParam("token", equalTo(apiKey))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        // fake JSON response
                        .withBody("""
                                {
                                  "marketCapitalization": 1234.56,
                                  "shareOutstanding": 987.65,
                                  "someExtraField": "ignored"
                                }
                                """)));

        // call the method HTTP GET -> WireMock -> WireMock returns JSON -> Jackson -> parse -> DTO
        FinnhubCompanyProfileResponse resp = client.getCompanyProfile2(symbol, apiKey);

        // assert: check parsing
        assertNotNull(resp);
        assertEquals(1234.56, resp.getMarketCapitalization());
        assertEquals(987.65, resp.getShareOutstanding());

        // assert: check whether the HTTP call was made
        verify(1, getRequestedFor(urlPathEqualTo("/stock/profile2"))
                .withQueryParam("symbol", equalTo(symbol))
                .withQueryParam("token", equalTo(apiKey)));
    }
}
