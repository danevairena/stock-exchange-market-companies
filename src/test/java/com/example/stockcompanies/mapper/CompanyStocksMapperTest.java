package com.example.stockcompanies.mapper;

import com.example.stockcompanies.dto.CompanyStocksResponse;
import com.example.stockcompanies.dto.FinnhubCompanyProfileResponse;
import com.example.stockcompanies.model.Company;
import com.example.stockcompanies.model.CompanyStock;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CompanyStocksMapperTest {

    // use generated mapstruct implementation
    private final CompanyStocksMapper mapper =
            Mappers.getMapper(CompanyStocksMapper.class);


    @Test
    void fromCompany_shouldMapAllCompanyFields_andIgnoreStockFields() {

        // given company entity with id set via reflection
        Instant createdAt = Instant.now();

        Company company = new Company(
                "Mega Inc",
                "MEGA",
                "BG",
                "https://mega.example",
                "contact@mega.example",
                createdAt
        );

        ReflectionTestUtils.setField(company, "id", 7L);

        // when mapping
        CompanyStocksResponse dto = mapper.fromCompany(company);

        // then company fields should be mapped
        assertNotNull(dto);

        assertEquals(7L, dto.getId());
        assertEquals("Mega Inc", dto.getName());
        assertEquals("BG", dto.getCountry());
        assertEquals("MEGA", dto.getSymbol());
        assertEquals("https://mega.example", dto.getWebsite());
        assertEquals("contact@mega.example", dto.getEmail());
        assertEquals(createdAt, dto.getCreatedAt());

        // then enrichment fields should remain null
        assertNull(dto.getMarketCapitalization());
        assertNull(dto.getShareOutstanding());
    }


    @Test
    void enrichFromStock_shouldUpdateOnlyStockFields() {

        // given existing dto and stock snapshot
        CompanyStocksResponse dto = new CompanyStocksResponse();
        dto.setId(1L);

        Company company = new Company(
                "Mega Inc",
                "MEGA",
                "BG",
                null,
                "mail",
                Instant.now()
        );

        CompanyStock stock = new CompanyStock(
                company,
                LocalDate.now(),
                555.0,
                111.0
        );

        // when enriching
        mapper.enrichFromStock(stock, dto);

        // then only enrichment fields should change
        assertEquals(1L, dto.getId());
        assertEquals(555.0, dto.getMarketCapitalization());
        assertEquals(111.0, dto.getShareOutstanding());
    }


    @Test
    void enrichFromFinnhub_shouldUpdateOnlyFinnhubFields() {

        // given dto and mocked finnhub response
        CompanyStocksResponse dto = new CompanyStocksResponse();
        dto.setId(2L);

        FinnhubCompanyProfileResponse finnhub = mock(FinnhubCompanyProfileResponse.class);

        when(finnhub.getMarketCapitalization()).thenReturn(999.0);
        when(finnhub.getShareOutstanding()).thenReturn(333.0);

        // when enriching
        mapper.enrichFromFinnhub(finnhub, dto);

        // then enrichment fields should change
        assertEquals(999.0, dto.getMarketCapitalization());
        assertEquals(333.0, dto.getShareOutstanding());
    }


    @Test
    void toResponse_withStock_shouldCombineCompanyAndStock() {

        // given company and stock snapshot
        Instant createdAt = Instant.now();

        Company company = new Company(
                "Mega Inc",
                "MEGA",
                "BG",
                "site",
                "mail",
                createdAt
        );

        ReflectionTestUtils.setField(company, "id", 10L);

        CompanyStock stock = new CompanyStock(
                company,
                LocalDate.now(),
                12.0,
                34.0
        );

        // when mapping
        CompanyStocksResponse dto =
                mapper.toResponse(company, stock);

        // then all fields should be present
        assertEquals(10L, dto.getId());
        assertEquals("Mega Inc", dto.getName());
        assertEquals(12.0, dto.getMarketCapitalization());
        assertEquals(34.0, dto.getShareOutstanding());
    }


    @Test
    void toResponse_withFinnhub_shouldCombineCompanyAndFinnhub() {

        // given company and finnhub response
        Company company = new Company(
                "Mega Inc",
                "MEGA",
                "BG",
                null,
                "mail",
                Instant.now()
        );

        ReflectionTestUtils.setField(company, "id", 11L);

        FinnhubCompanyProfileResponse finnhub = mock(FinnhubCompanyProfileResponse.class);

        when(finnhub.getMarketCapitalization()).thenReturn(56.0);
        when(finnhub.getShareOutstanding()).thenReturn(78.0);

        // when mapping
        CompanyStocksResponse dto =
                mapper.toResponse(company, finnhub);

        // then all fields should be present
        assertEquals(11L, dto.getId());
        assertEquals(56.0, dto.getMarketCapitalization());
        assertEquals(78.0, dto.getShareOutstanding());
    }

}