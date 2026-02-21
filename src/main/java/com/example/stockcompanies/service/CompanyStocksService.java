package com.example.stockcompanies.service;

import com.example.stockcompanies.dto.CompanyStocksResponse;
import com.example.stockcompanies.dto.FinnhubCompanyProfileResponse;
import com.example.stockcompanies.client.FinnhubFeignClient;
import com.example.stockcompanies.mapper.CompanyStocksMapper;
import com.example.stockcompanies.model.Company;
import com.example.stockcompanies.model.CompanyStock;
import com.example.stockcompanies.repository.CompanyRepository;
import com.example.stockcompanies.repository.CompanyStockRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class CompanyStocksService {
    private final CompanyRepository companyRepository;
    private final CompanyStockRepository companyStockRepository;
    private final FinnhubFeignClient finnhubClient;
    private final CompanyStocksMapper mapper;

    @Value("${finnhub.api-key}")
    private String apiKey;

    // Constructor Injection - Spring automatically provides the dependencies
    public CompanyStocksService(
            CompanyRepository companyRepository,
            CompanyStockRepository companyStockRepository,
            FinnhubFeignClient finnhubClient,
            CompanyStocksMapper mapper) {

        this.companyRepository = companyRepository;
        this.companyStockRepository = companyStockRepository;
        this.finnhubClient = finnhubClient;
        this.mapper = mapper;
    }

    public CompanyStocksResponse getCompanyStocks(Long companyId) {

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
