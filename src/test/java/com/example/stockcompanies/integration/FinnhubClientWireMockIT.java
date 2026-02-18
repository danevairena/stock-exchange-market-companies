package com.example.stockcompanies.integration;

import com.example.stockcompanies.dto.FinnhubCompanyProfileResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// check that an HTTP call is made, URL is correct, JSON is parsed correctly
// without actually calling Finnhub
class FinnhubClientWireMockIT {

    // starts fake HTTP server
    // dynamicPort() WireMock chooses a free port
    static WireMockServer wm;

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

    // integration test
    @Test
    void getCompanyProfile2_shouldReturnParsedResponse() throws Exception {
        // arrange
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

        // create the real client
        FinnhubClient client = new FinnhubClient();

        // setField - Spring usually sets it but there is no SpringBootTest, so we set it manually
        setField(client, "baseUrl", "http://localhost:" + wm.port());
        setField(client, "apiKey", apiKey);

        // call the method HTTP GET -> WireMock -> WireMock returns JSON -> Jackson -> parse -> DTO
        FinnhubCompanyProfileResponse resp = client.getCompanyProfile2(symbol);

        // assert: check parsing
        assertNotNull(resp);
        assertEquals(1234.56, resp.getMarketCapitalization());
        assertEquals(987.65, resp.getShareOutstanding());

        // assert: check whether the HTTP call was made
        verify(1, getRequestedFor(urlPathEqualTo("/stock/profile2"))
                .withQueryParam("symbol", equalTo(symbol))
                .withQueryParam("token", equalTo(apiKey)));
    }

    // helper method
    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}