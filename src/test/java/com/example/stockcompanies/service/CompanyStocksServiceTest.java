package com.example.stockcompanies.service;

import com.example.stockcompanies.client.FinnhubFeignClient;
import com.example.stockcompanies.dto.CompanyStocksResponse;
import com.example.stockcompanies.dto.FinnhubCompanyProfileResponse;
import com.example.stockcompanies.mapper.CompanyStocksMapper;
import com.example.stockcompanies.model.Company;
import com.example.stockcompanies.model.CompanyStock;
import com.example.stockcompanies.repository.CompanyRepository;
import com.example.stockcompanies.repository.CompanyStockRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyStocksServiceTest {

    @Mock CompanyRepository companyRepository;
    @Mock CompanyStockRepository companyStockRepository;
    @Mock FinnhubFeignClient finnhubClient;
    @Mock CompanyStocksMapper mapper;

    @InjectMocks CompanyStocksService service;

    @Test
    void getCompanyStocks_whenCompanyNotFound_shouldThrow() {
        long companyId = 42L;
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.getCompanyStocks(companyId)
        );
        assertEquals("Company with id 42 not found", ex.getMessage());

        verify(companyRepository).findById(companyId);

        // service should fail fast and not call downstream dependencies
        verifyNoInteractions(finnhubClient, mapper);
        verify(companyStockRepository, never()).save(any());
        verify(companyStockRepository, never()).findByCompanyIdAndFetchDate(anyLong(), any());
    }

    @Test
    void getCompanyStocks_whenCacheExists_shouldReturnCache() {
        long companyId = 7L;

        Company company = mock(Company.class);
        CompanyStock cached = mock(CompanyStock.class);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(companyStockRepository.findByCompanyIdAndFetchDate(eq(companyId), any(LocalDate.class)))
                .thenReturn(Optional.of(cached));

        CompanyStocksResponse mapped = new CompanyStocksResponse();
        mapped.setId(companyId);

        // only stub what is actually used: the mapper call that produces the response
        when(mapper.toResponse(company, cached)).thenReturn(mapped);

        CompanyStocksResponse result = service.getCompanyStocks(companyId);

        assertNotNull(result);
        assertEquals(companyId, result.getId());

        // with cache hit, external API should not be called and nothing should be saved
        verifyNoInteractions(finnhubClient);
        verify(companyStockRepository, never()).save(any());

        verify(companyRepository).findById(companyId);
        verify(companyStockRepository).findByCompanyIdAndFetchDate(eq(companyId), any(LocalDate.class));
        verify(mapper).toResponse(company, cached);
        verifyNoMoreInteractions(mapper);
    }

    @Test
    void getCompanyStocks_whenCacheMissing_shouldFetchAndSave() {
        // apiKey is injected via @Value in production; set it explicitly for a unit test
        ReflectionTestUtils.setField(service, "apiKey", "test-api-key");

        long companyId = 99L;
        LocalDate today = LocalDate.now();

        Company company = mock(Company.class);

        // service needs the symbol to call Finnhub
        // stub only what is required
        when(company.getSymbol()).thenReturn("MEGA");

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(companyStockRepository.findByCompanyIdAndFetchDate(eq(companyId), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        FinnhubCompanyProfileResponse finnhubResp = mock(FinnhubCompanyProfileResponse.class);
        when(finnhubResp.getMarketCapitalization()).thenReturn(555.0);
        when(finnhubResp.getShareOutstanding()).thenReturn(111.0);

        when(finnhubClient.getCompanyProfile2("MEGA", "test-api-key"))
                .thenReturn(finnhubResp);

        // save should return the stored entity instance
        when(companyStockRepository.save(any(CompanyStock.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CompanyStocksResponse mapped = new CompanyStocksResponse();
        mapped.setId(companyId);
        mapped.setMarketCapitalization(555.0);
        mapped.setShareOutstanding(111.0);

        // the mapper is called with the company and the saved CompanyStock
        when(mapper.toResponse(eq(company), any(CompanyStock.class))).thenReturn(mapped);

        CompanyStocksResponse result = service.getCompanyStocks(companyId);

        assertNotNull(result);
        assertEquals(companyId, result.getId());
        assertEquals(Double.valueOf(555.0), result.getMarketCapitalization());
        assertEquals(Double.valueOf(111.0), result.getShareOutstanding());

        verify(finnhubClient).getCompanyProfile2("MEGA", "test-api-key");

        ArgumentCaptor<CompanyStock> captor = ArgumentCaptor.forClass(CompanyStock.class);
        verify(companyStockRepository).save(captor.capture());

        CompanyStock saved = captor.getValue();
        assertNotNull(saved);

        // service most likely uses LocalDate.now(); align assertion with that
        assertEquals(today, saved.getFetchDate());
        assertEquals(Double.valueOf(555.0), saved.getMarketCapitalization());
        assertEquals(Double.valueOf(111.0), saved.getShareOutstanding());

        verify(companyRepository).findById(companyId);
        verify(companyStockRepository).findByCompanyIdAndFetchDate(eq(companyId), any(LocalDate.class));
        verify(mapper).toResponse(eq(company), any(CompanyStock.class));
        verifyNoMoreInteractions(mapper);
    }
}