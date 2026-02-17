package com.example.stockcompanies.repository;

import com.example.stockcompanies.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// repository layer for Company entity that provides CRUD operations and db access
@Repository
//extends JpaRepository<Company, Long> -> Entity is Company type, primary key is type Long
public interface CompanyRepository extends JpaRepository<Company, Long> {
    boolean existsBySymbol(String symbol);
}