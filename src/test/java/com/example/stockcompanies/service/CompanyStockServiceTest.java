package com.example.stockcompanies.service;

import com.example.stockcompanies.dto.CompanyStocksResponse;
import com.example.stockcompanies.dto.FinnhubCompanyProfileResponse;
import com.example.stockcompanies.integration.FinnhubClient;
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

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyStocksServiceTest {

    @Mock CompanyRepository companyRepository;
    @Mock CompanyStockRepository companyStockRepository;
    @Mock FinnhubClient finnhubClient;

    @InjectMocks CompanyStocksService service;

    // company not found
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

        // do not call finnfub
        verifyNoInteractions(finnhubClient);

        // do not save
        verify(companyStockRepository, never()).save(any());

        // do not check cache when there is no company
        verify(companyStockRepository, never()).findByCompanyIdAndFetchDate(anyLong(), any());
    }

    // cache exists
    @Test
    void getCompanyStocks_whenCacheExists_shouldReturnCache() {
        long companyId = 7L;
        LocalDate today = LocalDate.now();

        Company company = mock(Company.class);
        when(company.getId()).thenReturn(companyId);
        when(company.getName()).thenReturn("Acme Inc");
        when(company.getSymbol()).thenReturn("ACME");
        when(company.getCountry()).thenReturn("US");
        when(company.getWebsite()).thenReturn("https://acme.example");
        when(company.getEmail()).thenReturn("info@acme.example");
        when(company.getCreatedAt()).thenReturn(null);

        CompanyStock cached = mock(CompanyStock.class);
        when(cached.getMarketCapitalization()).thenReturn(123.45);
        when(cached.getShareOutstanding()).thenReturn(67.89);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(companyStockRepository.findByCompanyIdAndFetchDate(companyId, today))
                .thenReturn(Optional.of(cached));

        CompanyStocksResponse result = service.getCompanyStocks(companyId);

        // returns cached data
        assertNotNull(result);
        assertEquals(companyId, result.getId());
        assertEquals("Acme Inc", result.getName());
        assertEquals("ACME", result.getSymbol());
        assertEquals("US", result.getCountry());
        assertEquals("https://acme.example", result.getWebsite());
        assertEquals("info@acme.example", result.getEmail());
        assertEquals(123.45, result.getMarketCapitalization());
        assertEquals(67.89, result.getShareOutstanding());

        // do not finnhub
        verifyNoInteractions(finnhubClient);

        // do not save-ва
        verify(companyStockRepository, never()).save(any());

        verify(companyRepository).findById(companyId);
        verify(companyStockRepository).findByCompanyIdAndFetchDate(companyId, today);
    }

    // cache is missing
    @Test
    void getCompanyStocks_whenCacheMissing_shouldFetchAndSave() {
        long companyId = 99L;
        LocalDate today = LocalDate.now();

        Company company = mock(Company.class);
        when(company.getId()).thenReturn(companyId);
        when(company.getName()).thenReturn("MegaCorp");
        when(company.getSymbol()).thenReturn("MEGA");
        when(company.getCountry()).thenReturn("DE");
        when(company.getWebsite()).thenReturn("https://megacorp.example");
        when(company.getEmail()).thenReturn("contact@megacorp.example");
        when(company.getCreatedAt()).thenReturn(null);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(companyStockRepository.findByCompanyIdAndFetchDate(companyId, today))
                .thenReturn(Optional.empty());

        FinnhubCompanyProfileResponse finnhubResp = mock(FinnhubCompanyProfileResponse.class);
        when(finnhubResp.getMarketCapitalization()).thenReturn(555.0);
        when(finnhubResp.getShareOutstanding()).thenReturn(111.0);

        when(finnhubClient.getCompanyProfile2("MEGA")).thenReturn(finnhubResp);

        // want to test companyStockRepository.save(companyStock), but companyStock is created inside the service
        // don't have a direct reference to this object in the test, so ArgumentCaptor allows us to "steal" it
        ArgumentCaptor<CompanyStock> captor = ArgumentCaptor.forClass(CompanyStock.class);

        // should return the same object is the same object
        when(companyStockRepository.save(any(CompanyStock.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CompanyStocksResponse result = service.getCompanyStocks(companyId);

        // return response
        assertNotNull(result);
        assertEquals(companyId, result.getId());
        assertEquals("MegaCorp", result.getName());
        assertEquals("MEGA", result.getSymbol());
        assertEquals(555.0, result.getMarketCapitalization());
        assertEquals(111.0, result.getShareOutstanding());

        // call finnhubClient
        verify(finnhubClient).getCompanyProfile2("MEGA");

        // save CompanyStock and check fields
        verify(companyStockRepository).save(captor.capture());
        CompanyStock saved = captor.getValue();

        assertNotNull(saved);
        assertSame(company, saved.getCompany());
        assertEquals(today, saved.getFetchDate());
        assertEquals(555.0, saved.getMarketCapitalization());
        assertEquals(111.0, saved.getShareOutstanding());

        verify(companyRepository).findById(companyId);
        verify(companyStockRepository).findByCompanyIdAndFetchDate(companyId, today);
    }
}
