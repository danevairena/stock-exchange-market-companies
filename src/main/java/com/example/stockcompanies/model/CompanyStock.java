package com.example.stockcompanies.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

//create entity for database table with name company_stocks
@Entity
@Table(name = "company_stocks", uniqueConstraints = @UniqueConstraint(name = "uk_company_stock_company_date", columnNames = {"company_id", "fetch_date"}))

public class CompanyStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FetchType.LAZY -> when loading CompanyStock, Hibernate does NOT automatically load Company until you need it
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    // the foreign key column in company_stocks is called company_id
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    // caching key (1 per day)
    // ifs there is a record for companyId and today's date return it and don't call Finnhub.
    @Column(name = "fetch_date", nullable = false)
    private LocalDate fetchDate;
    // markerCapitalization and shareOutstanding are coming from Finnhub endpoint
    @Column(nullable = false)
    private Double marketCapitalization;
    @Column(nullable = false)
    private Double shareOutstanding;
    // createdAt – when the snapshot was saved to our system
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected CompanyStock() {}

    public CompanyStock(Company company, LocalDate fetchDate, Double marketCapitalization, Double shareOutstanding) {
        // snapshot validation - if any field is null throw NullPointerException immediately and don’t allow invalid records to reach the database
        this.company = Objects.requireNonNull(company);
        this.fetchDate = Objects.requireNonNull(fetchDate);
        this.marketCapitalization = Objects.requireNonNull(marketCapitalization);
        this.shareOutstanding = Objects.requireNonNull(shareOutstanding);
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    // entity is immutable after recording in db - only getters

    public Long getId() { return id; }
    public Company getCompany() { return company; }
    public LocalDate getFetchDate() { return fetchDate; }
    public Double getMarketCapitalization() { return marketCapitalization; }
    public Double getShareOutstanding() { return shareOutstanding; }
    public Instant getCreatedAt() { return createdAt; }
}
