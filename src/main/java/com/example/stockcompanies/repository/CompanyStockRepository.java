package com.example.stockcompanies.repository;

import com.example.stockcompanies.model.CompanyStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

// repository layer for Company entity that provides CRUD operations and db access
@Repository
//extends JpaRepository<Company, Long> -> Entity is Company type, primary key is type Long
public interface CompanyStockRepository extends JpaRepository<CompanyStock, Long> {
    // cache lookup - find today's snapshot for a company
    Optional<CompanyStock> findByCompanyIdAndFetchDate(Long companyId, LocalDate fetchDate);
}

