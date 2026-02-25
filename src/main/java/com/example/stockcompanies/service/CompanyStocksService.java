package com.example.stockcompanies.service;

import com.example.stockcompanies.dto.CompanyStocksResponse;
import com.example.stockcompanies.dto.FinnhubCompanyProfileResponse;
import com.example.stockcompanies.client.FinnhubFeignClient;
import com.example.stockcompanies.mapper.CompanyStocksMapper;
import com.example.stockcompanies.model.Company;
import com.example.stockcompanies.model.CompanyStock;
import com.example.stockcompanies.repository.CompanyRepository;
import com.example.stockcompanies.repository.CompanyStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CompanyStocksService {
    private final CompanyRepository companyRepository;
    private final CompanyStockRepository companyStockRepository;
    private final FinnhubFeignClient finnhubClient;
    private final CompanyStocksMapper mapper;

    @Value("${finnhub.api-key}")
    private String apiKey;

    public CompanyStocksResponse getCompanyStocks(Long companyId) {

        // basic input validation
        if (companyId == null) {
            throw new IllegalArgumentException("companyId is required");
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Company with id " + companyId + " not found"));
        LocalDate today = LocalDate.now();

        // check cache - is there already stock data for this company for today
        CompanyStock companyStock =
                companyStockRepository
                        .findByCompanyIdAndFetchDate(
                                companyId,
                                today)
                        .orElseGet(() ->
                                fetchAndSave(company, today));

        // return response
        return mapToResponse(company, companyStock);
    }

    private CompanyStock fetchAndSave(
            Company company,
            LocalDate date) {

        FinnhubCompanyProfileResponse finnhub =
                finnhubClient.getCompanyProfile2(company.getSymbol(), apiKey);

        // avoid NullPointerException if Finnhub return null
        if (finnhub == null) {
            throw new IllegalStateException("Finnhub returned empty response for symbol " + company.getSymbol());
        }

        CompanyStock companyStock =
                new CompanyStock(
                        company,
                        date,
                        finnhub.getMarketCapitalization(),
                        finnhub.getShareOutstanding());
        return companyStockRepository.save(companyStock);
    }

    private CompanyStocksResponse mapToResponse(
            Company company,
            CompanyStock stock) {
        // return response
        return mapper.toResponse(company, stock);
    }
}
