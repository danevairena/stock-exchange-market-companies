package com.example.stockcompanies.service;

import com.example.stockcompanies.dto.CompanyStocksResponse;
import com.example.stockcompanies.dto.FinnhubCompanyProfileResponse;
import com.example.stockcompanies.integration.FinnhubClient;
import com.example.stockcompanies.model.Company;
import com.example.stockcompanies.model.CompanyStock;
import com.example.stockcompanies.repository.CompanyRepository;
import com.example.stockcompanies.repository.CompanyStockRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class CompanyStocksService {
    // Service depends on database, cache table and Finnhub API
    private final CompanyRepository companyRepository;
    private final CompanyStockRepository companyStockRepository;
    private final FinnhubClient finnhubClient;

    // Constructor Injection - Spring automatically provides the dependencies
    public CompanyStocksService(
            CompanyRepository companyRepository,
            CompanyStockRepository companyStockRepository,
            FinnhubClient finnhubClient) {

        this.companyRepository = companyRepository;
        this.companyStockRepository = companyStockRepository;
        this.finnhubClient = finnhubClient;
    }

    public CompanyStocksResponse getCompanyStocks(Long companyId) {

        // find company in database
        // if there is no Company Spring throws an exception.
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Company with id " + companyId + " not found"));
        // get today's date
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

        // call Finnhub
        FinnhubCompanyProfileResponse finnhub =
                finnhubClient
                        .getCompanyProfile2(
                                company.getSymbol());
        // creates a CompanyStock entity
        CompanyStock companyStock =
                new CompanyStock(
                        company,
                        date,
                        finnhub.getMarketCapitalization(),
                        finnhub.getShareOutstanding());
        // save the snapshot in db
        return companyStockRepository.save(companyStock);
    }

    // CompanyStocksResponse method creates a response object
    private CompanyStocksResponse mapToResponse(
            Company company,
            CompanyStock stock) {
        // create DTO
        CompanyStocksResponse response =
                new CompanyStocksResponse();
        // company fields
        response.setId(company.getId());
        response.setName(company.getName());
        response.setSymbol(company.getSymbol());
        response.setCountry(company.getCountry());
        response.setWebsite(company.getWebsite());
        response.setEmail(company.getEmail());
        response.setCreatedAt(company.getCreatedAt());
        // stock fields
        response.setMarketCapitalization(
                stock.getMarketCapitalization());

        response.setShareOutstanding(
                stock.getShareOutstanding());

        return response;
    }
}
