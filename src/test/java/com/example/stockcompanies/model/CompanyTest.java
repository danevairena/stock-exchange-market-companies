package com.example.stockcompanies.model;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class CompanyTest {

    @Test
    void onCreate_shouldSetCreatedAt_whenNull() {

        // given company with null createdAt
        Company company = new Company("Apple", "AAPL", "US", null, "a@b.com", null);

        // when prePersist hook runs
        ReflectionTestUtils.invokeMethod(company, "onCreate");

        // then createdAt should be set
        assertNotNull(company.getCreatedAt());
    }

    @Test
    void updateMethods_shouldChangeFields() {

        // given company
        Instant createdAt = Instant.now();
        Company company = new Company("Apple", "AAPL", "US", null, "a@b.com", createdAt);

        // when updating fields
        company.updateName("Tesla");
        company.updateSymbol("TSLA");
        company.updateCountry("BG");
        company.updateWebsite("https://example.com");
        company.updateEmail("t@t.com");

        // then getters should return updated values
        assertEquals("Tesla", company.getName());
        assertEquals("TSLA", company.getSymbol());
        assertEquals("BG", company.getCountry());
        assertEquals("https://example.com", company.getWebsite());
        assertEquals("t@t.com", company.getEmail());

        // then createdAt should not be changed by updates
        assertEquals(createdAt, company.getCreatedAt());
    }

    @Test
    void equals_shouldReturnFalse_forNull() {

        // given company
        Company company = new Company("Apple", "AAPL", "US", null, "a@b.com", Instant.now());

        // then null should not be equal
        assertNotEquals(null, company);
    }

    @Test
    void equals_shouldCompareById_whenIdIsSet() {

        // given two entities with same id
        Company c1 = new Company("Apple", "AAPL", "US", null, "a@b.com", Instant.now());
        Company c2 = new Company("Tesla", "TSLA", "BG", null, "t@t.com", Instant.now());

        ReflectionTestUtils.setField(c1, "id", 10L);
        ReflectionTestUtils.setField(c2, "id", 10L);

        // then they should be equal by id
        assertEquals(c1, c2);
    }

    @Test
    void hashCode_shouldBeSame_forSameId() {

        // given two entities with same id
        Company c1 = new Company("Apple", "AAPL", "US", null, "a@b.com", Instant.now());
        Company c2 = new Company("Tesla", "TSLA", "BG", null, "t@t.com", Instant.now());

        ReflectionTestUtils.setField(c1, "id", 99L);
        ReflectionTestUtils.setField(c2, "id", 99L);

        // then hash codes should match
        assertEquals(c1.hashCode(), c2.hashCode());
    }
}