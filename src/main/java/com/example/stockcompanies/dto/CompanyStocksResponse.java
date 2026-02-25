package com.example.stockcompanies.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

// CompanyStocksResponse is the DTO class that the endpoint returns to the client
// it combines data from database (Company) and Finnhub or cache (CompanyStock)
// Lombok annotations
@Getter
@Builder
@Jacksonized
public class CompanyStocksResponse {

    // setters because service fills in the response
    // Company entity fields
    private Long id;
    private String name;
    private String country;
    private String symbol;
    private String website;
    private String email;
    private Instant createdAt;

    // CompanyStock/Finnhub fields
    private Double marketCapitalization;
    private Double shareOutstanding;

}
