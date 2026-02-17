package com.example.stockcompanies.dto;

import java.time.Instant;

// CompanyStocksResponse is the DTO class that the endpoint returns to the client
// it combines data from database (Company) and Finnhub or cache (CompanyStock)
public class CompanyStocksResponse {
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

    // getters and setters
    // setters because service fills in the response
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Double getMarketCapitalization() { return marketCapitalization; }
    public void setMarketCapitalization(Double marketCapitalization) { this.marketCapitalization = marketCapitalization; }

    public Double getShareOutstanding() { return shareOutstanding; }
    public void setShareOutstanding(Double shareOutstanding) { this.shareOutstanding = shareOutstanding; }
}
