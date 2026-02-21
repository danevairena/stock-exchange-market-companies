package com.example.stockcompanies.service;

import com.example.stockcompanies.client.FinnhubFeignClient;
import com.example.stockcompanies.dto.FinnhubCompanyProfileResponse;
import com.example.stockcompanies.model.Company;
import com.example.stockcompanies.repository.CompanyRepository;
import com.example.stockcompanies.repository.CompanyStockRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Testcontainers
@SpringBootTest
@ActiveProfiles("it")
// keeps tests isolated when you have more ITs
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class CompanyStocksServiceIT {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("stockcompanies_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        // wire Spring datasource to the Testcontainers Postgres
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // ensure Postgres driver/dialect are used (prevents H2 from being auto-selected in tests)
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @Autowired private CompanyStocksService companyStocksService;
    @Autowired private CompanyRepository companyRepository;
    @Autowired private CompanyStockRepository companyStockRepository;

    @MockitoBean private FinnhubFeignClient finnhubFeignClient;

    @Test
    void cache_miss_calls_finnhub_and_saves_snapshot_then_cache_hit_uses_db() {
        // persist a Company in the real Postgres container
        Company company = companyRepository.save(
                new Company("Test Co", "TST", "US", "https://tst.com", "tst@tst.com", Instant.now())
        );

        FinnhubCompanyProfileResponse finnhub = new FinnhubCompanyProfileResponse();
        finnhub.setMarketCapitalization(111.0);
        finnhub.setShareOutstanding(22.0);

        when(finnhubFeignClient.getCompanyProfile2(eq("TST"), anyString()))
                .thenReturn(finnhub);

        // cache miss -> calls Finnhub -> stores snapshot in DB
        var first = companyStocksService.getCompanyStocks(company.getId());

        // values returned + one row persisted + one external call
        assertThat(first.getSymbol()).isEqualTo("TST");
        assertThat(first.getMarketCapitalization()).isEqualTo(111.0);
        assertThat(first.getShareOutstanding()).isEqualTo(22.0);

        assertThat(companyStockRepository.findAll()).hasSize(1);
        verify(finnhubFeignClient, times(1)).getCompanyProfile2(eq("TST"), anyString());

        // cache hit -> should use DB, not call Finnhub again
        var second = companyStocksService.getCompanyStocks(company.getId());

        // same values, no more external calls
        assertThat(second.getMarketCapitalization()).isEqualTo(111.0);
        assertThat(second.getShareOutstanding()).isEqualTo(22.0);
        verifyNoMoreInteractions(finnhubFeignClient);
    }
}