package com.example.stockcompanies.dto;

// DTO -> Data transfer object
// accept JSON from Finnhub API and convert it to a Java object
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Lombok annotations
@JsonIgnoreProperties(ignoreUnknown = true)
@Setter
@Getter
@NoArgsConstructor
public class FinnhubCompanyProfileResponse {

    // setter for marketCapitalization (used by Jackson)
    private Double marketCapitalization;
    // setter for shareOutstanding (used by Jackson)
    private Double shareOutstanding;
}
