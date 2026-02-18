package com.example.stockcompanies.service;

import com.example.stockcompanies.model.Company;
import com.example.stockcompanies.repository.CompanyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyService {
    // CompanyService depends on CompanyRepository
    // Constructor injection ensures that the dependency will always be available, allowing the field to be immutable
    private final CompanyRepository companyRepository;
    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    // POST /companies
    public Company createCompany(Company company) {
        // input validation to avoid NullPointerException (empty body)
        if (company == null) {
            throw new IllegalArgumentException("Company body is required");
        }
        // getting required fields, validation and normalization
        // requireNonBlank -> ensures that the value is not null and is not an empty string
        String name = requireNonBlank(company.getName(), "Name is required").trim();
        String country = requireNonBlank(company.getCountry(), "Country is required").trim().toUpperCase();
        String symbol = requireNonBlank(company.getSymbol(), "Symbol is required").trim().toUpperCase();
        String email = requireNonBlank(company.getEmail(), "Email is required").trim();
        // checking for unique symbol
        if (companyRepository.existsBySymbol(symbol)) {
            throw new IllegalStateException("Company with symbol " + symbol + " already exists");
        }
        // recording normalized values in the entity
        company.updateName(name);
        company.updateCountry(country);
        company.updateSymbol(symbol);
        company.updateEmail(email);
        // optional field for website
        if (company.getWebsite() != null) {
            company.updateWebsite(company.getWebsite().trim());
        }
        // createdAt should be set by @PrePersist in Company
        // record company in db
        return companyRepository.save(company);
    }

    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    public Company updateCompany(Long id, Company updatedData) {
        // input validation to avoid NullPointerException (empty body)
        if (updatedData == null) {
            throw new IllegalArgumentException("Company body is required");
        }
        // find company by id
        // no update if company does not exist
        Company existing = companyRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Company with id " + id + " not found"));
        // getting required fields, validation and normalization
        // requireNonBlank -> ensures that the value is not null and is not an empty string
        String name = requireNonBlank(updatedData.getName(), "Name is required").trim();
        String country = requireNonBlank(updatedData.getCountry(), "Country is required").trim().toUpperCase();
        String symbol = requireNonBlank(updatedData.getSymbol(), "Symbol is required").trim().toUpperCase();
        String email = requireNonBlank(updatedData.getEmail(), "Email is required").trim();
        // checking for unique symbol
        if (!symbol.equalsIgnoreCase(existing.getSymbol()) && companyRepository.existsBySymbol(symbol)) {
            throw new IllegalStateException("Company with symbol " + symbol + " already exists");
        }
        // recording normalized values in the existing entity (DB)
        existing.updateName(name);
        existing.updateCountry(country);
        existing.updateSymbol(symbol);
        existing.updateEmail(email);
        // website can be null
        existing.updateWebsite(updatedData.getWebsite() != null ? updatedData.getWebsite().trim() : null);
        // save changes - UPDATE db record
        return companyRepository.save(existing);
    }
    // helper function used for check input
    private String requireNonBlank(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}