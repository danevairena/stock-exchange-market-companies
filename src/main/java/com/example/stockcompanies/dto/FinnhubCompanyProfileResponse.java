package com.example.stockcompanies.dto;

// DTO -> Data transfer object
// accept JSON from Finnhub API and convert it to a Java object
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// @JsonIgnoreProperties -> ignore all other fields that do not exist in the class
@JsonIgnoreProperties(ignoreUnknown = true)
public class FinnhubCompanyProfileResponse {

    private Double marketCapitalization;
    private Double shareOutstanding;

    public Double getMarketCapitalization() {
        return marketCapitalization;
    }

    // setter for marketCapitalization (used by Jackson)
    public void setMarketCapitalization(Double marketCapitalization) {
        this.marketCapitalization = marketCapitalization;
    }

    public Double getShareOutstanding() {
        return shareOutstanding;
    }

    // setter for shareOutstanding (used by Jackson)
    public void setShareOutstanding(Double shareOutstanding) {
        this.shareOutstanding = shareOutstanding;
    }
}
