package com.example.stockcompanies.controller;

import com.example.stockcompanies.dto.CompanyStocksResponse;
import com.example.stockcompanies.service.CompanyStocksService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// @RestController returns JSON
@RestController
// all endpoints in this controller start with /company-stocks
@RequestMapping("/company-stocks")
public class CompanyStocksController {

    // Constructor injection ensures that the dependency will always be available, allowing the field to be immutable
    private final CompanyStocksService companyStocksService;

    public CompanyStocksController(CompanyStocksService companyStocksService) {
        this.companyStocksService = companyStocksService;
    }

    //@GetMapping("/{companyId}") -> gets companyId from URL and returns CompanyStocksResponse which contains Company and stock data
    @GetMapping("/{companyId}")
    public CompanyStocksResponse getCompanyStocks(@PathVariable Long companyId) {
        return companyStocksService.getCompanyStocks(companyId);
    }
}
