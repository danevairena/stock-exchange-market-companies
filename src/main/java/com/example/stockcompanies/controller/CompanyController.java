package com.example.stockcompanies.controller;

import com.example.stockcompanies.model.Company;
import com.example.stockcompanies.service.CompanyService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// RestController combines @Controller and @ResponseBody
// returns JSON directly
@RestController
// all endpoints in this controller start with /companies
@RequestMapping("/companies")
public class CompanyController {

    // Constructor injection ensures that the dependency will always be available, allowing the field to be immutable
    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    // POST /companies
    // when method completes successfully returns HTTP status 201 Created
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Company createCompany(@RequestBody Company company) {
        return companyService.createCompany(company);
    }

    @GetMapping
    public List<Company> getAllCompanies() {
        return companyService.getAllCompanies();
    }

    @PutMapping("/{id}")
    // @PathVariable Long id takes id from URL. Body is the new data
    public Company updateCompany(@PathVariable Long id, @RequestBody Company updatedData) {
        return companyService.updateCompany(id, updatedData);
    }
}
