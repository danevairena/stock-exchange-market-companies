package com.example.stockcompanies.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;


//create entity for the database
@Entity
// set table name
@Table(name = "companies")
public class Company {
    //primary key
    @Id
    // generating IDs in advance to optimize insert performance
    @SequenceGenerator(name = "company_seq", sequenceName = "company_seq", allocationSize = 20)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "company_seq")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String symbol;

    @Column(nullable = false)
    private String country;

    @Column
    private String website;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    // protected constructor without arguments
    protected Company() {}

    // constructor for new company
    public Company(String name, String symbol, String country, String website, String email, Instant createdAt) {
        this.name = name;
        this.symbol = symbol;
        this.country = country;
        this.website = website;
        this.email = email;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCountry() { return country; }
    public String getSymbol() { return symbol; }
    public String getWebsite() { return website; }
    public String getEmail() { return email; }
    public Instant getCreatedAt() { return createdAt; }

    // controlled setters
    public void updateName(String name) { this.name = name; }
    public void updateCountry(String country) { this.country = country; }
    public void updateSymbol(String symbol) { this.symbol = symbol; }
    public void updateWebsite(String website) { this.website = website; }
    public void updateEmail(String email) { this.email = email; }

    // compare entities by primary key
    @Override
    public boolean equals(Object o) {
        // check if the two variables reference to the same object in the memory
        if (this == o) return true;
        // check if object o is of type Company
        if (!(o instanceof Company)) return false;
        // cast object o as company object
        Company company = (Company) o;
        // if id is null - object is not persistent in the db
        // check if id is equal to object in the db
        return id != null && id.equals(company.id);
    }

    // guarantees correct behavior for HashSet and HashMap
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
