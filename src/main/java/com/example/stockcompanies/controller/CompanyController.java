package com.example.stockcompanies.controller;

import com.example.stockcompanies.model.Company;
import com.example.stockcompanies.service.CompanyService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// RestController combines @Controller and @ResponseBody
// returns JSON directly
@RestController
// all endpoints in this controller start with /companies
@RequestMapping("/companies")
public class CompanyController {
    // CompanyService depends on CompanyRepository
    // Constructor injection ensures that the dependency will always be available, allowing the field to be immutable
    private final CompanyService companyService;
    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    // POST /companies
    // @PostMapping accepts JSON body and maps it to Company, then returns the created company
    @PostMapping
    // when method completes successfully returns HTTP status 201 Created
    @ResponseStatus(HttpStatus.CREATED)
    // @RequestBody Company company -> take JSON from HTTP request body and convert it to java object Company
    public Company createCompany(@RequestBody Company company) {
        // calls the service layer
        return companyService.createCompany(company);
    }

    @GetMapping
    public List<Company> getAllCompanies() {
        return companyService.getAllCompanies();
    }

    @PutMapping("/{id}")
    // @PathVariable Long id takes id from URL. Body is the new data
    public Company updateCompany(@PathVariable Long id, @RequestBody Company updatedData) {
        // calls the service layer, which checks and updates
        return companyService.updateCompany(id, updatedData);
    }
}