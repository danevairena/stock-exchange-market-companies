package com.example.stockcompanies.client;

import com.example.stockcompanies.dto.FinnhubCompanyProfileResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("no-db")
@SpringBootTest(
        classes = FinnhubFeignClientWireMockIT.TestApp.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
class FinnhubFeignClientWireMockIT {

    static final WireMockServer wm =
            new WireMockServer(wireMockConfig().dynamicPort());

    @Autowired
    FinnhubFeignClient client;

    @SpringBootConfiguration
    @EnableFeignClients(clients = FinnhubFeignClient.class)
    @EnableAutoConfiguration(excludeName = {
            "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration",
            "org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration",
            "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration",
            "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration"
    })
    static class TestApp {}

    @BeforeAll
    static void startServer() {

        wm.start();

        // 🔥 CRITICAL FIX
        configureFor("localhost", wm.port());
    }

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {

        registry.add("finnhub.base-url",
                () -> "http://localhost:" + wm.port());

        registry.add("spring.cloud.openfeign.client.config.finnhub.url",
                () -> "http://localhost:" + wm.port());

        registry.add("finnhub.api-key",
                () -> "test-api-key");
    }

    @AfterAll
    static void stopServer() {

        wm.stop();
    }

    @Test
    void getCompanyProfile2_shouldReturnParsedResponse() {

        String symbol = "AAPL";
        String apiKey = "test-api-key";

        stubFor(get(urlPathEqualTo("/stock/profile2"))
                .withQueryParam("symbol", equalTo(symbol))
                .withQueryParam("token", equalTo(apiKey))
                .willReturn(okJson("""
                        {
                          "marketCapitalization": 1234.56,
                          "shareOutstanding": 987.65
                        }
                        """)));

        var resp = client.getCompanyProfile2(symbol, apiKey);

        assertNotNull(resp);

        assertEquals(1234.56,
                resp.getMarketCapitalization());

        assertEquals(987.65,
                resp.getShareOutstanding());

        verify(getRequestedFor(urlPathEqualTo("/stock/profile2")));
    }
}
