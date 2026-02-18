package com.example.stockcompanies.integration;

import com.example.stockcompanies.dto.FinnhubCompanyProfileResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

// @Component -> create an object of this class and manage it
@Component
public class FinnhubClient {

    // RestTemplate is an HTTP client that makes HTTP GET request to Finnhub and receives JSON
    private final RestTemplate restTemplate = new RestTemplate();

    // @Value -> Spring takes value from application.properties
    @Value("${finnhub.base-url}")
    private String baseUrl;

    @Value("${finnhub.api-key}")
    private String apiKey;

    //getCompanyProfile2 method takes symbol, calls Finnhub and returns DTO object
    public FinnhubCompanyProfileResponse getCompanyProfile2(String symbol) {

        String url = UriComponentsBuilder
                .fromHttpUrl(baseUrl)
                .path("/stock/profile2")
                .queryParam("symbol", symbol)
                .queryParam("token", apiKey)
                .toUriString();

        return restTemplate.getForObject(
                url,
                FinnhubCompanyProfileResponse.class
        );
    }
}
