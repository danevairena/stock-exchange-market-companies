package com.example.stockcompanies.service;

import com.example.stockcompanies.model.Company;
import com.example.stockcompanies.repository.CompanyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

// use the Mockito library to create mock objects and integrate it with other tools
@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {
    // creates mock of the repository
    @Mock
    CompanyRepository companyRepository;
    // creates a real CompanyService, but injects the mocked companyRepository into it
    @InjectMocks
    CompanyService companyService;

    // when you pass null
    // createCompany should throw IllegalArgumentException
    // the message should be exactly "Company body is required"
    // the repository should not be called at all (it should stop at the entrance)
    @Test
    void createCompany_whenBodyIsNull_shouldThrow() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> companyService.createCompany(null)
        );

        assertEquals("Company body is required", ex.getMessage());
        verifyNoInteractions(companyRepository);
    }

    // expected IllegalStateException
    // with message "Company with symbol AAPL already exists"
    // checks that existsBySymbol("AAPL") is called
    // save() should never be called because there is a conflict
    @Test
    void createCompany_whenSymbolAlreadyExists_shouldThrow() {
        // mock company object
        Company company = mock(Company.class);
        when(company.getName()).thenReturn("Apple");
        when(company.getCountry()).thenReturn("us");
        when(company.getSymbol()).thenReturn("aapl");
        when(company.getEmail()).thenReturn("test@apple.com");

        when(companyRepository.existsBySymbol("AAPL")).thenReturn(true);

        // createCompany is called and an exception is expected
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> companyService.createCompany(company)
        );
        // message should be "Company with symbol AAPL already exists"
        assertEquals("Company with symbol AAPL already exists", ex.getMessage());
        verify(companyRepository).existsBySymbol("AAPL");
        verify(companyRepository, never()).save(any());
    }

    // expects the service to
    // trim() on all texts
    // toUpperCase() for country and symbol
    @Test
    void createCompany_whenValid_shouldNormalizeAndSave() {
        Company company = mock(Company.class);
        when(company.getName()).thenReturn("  Apple  ");
        when(company.getCountry()).thenReturn("  us ");
        when(company.getSymbol()).thenReturn(" aapl ");
        when(company.getEmail()).thenReturn("  test@apple.com  ");
        when(company.getWebsite()).thenReturn("  https://apple.com  ");

        when(companyRepository.existsBySymbol("AAPL")).thenReturn(false);
        when(companyRepository.save(company)).thenReturn(company);

        Company saved = companyService.createCompany(company);

        //assertSame(company, saved) checks that the returned object is the same instance
        assertSame(company, saved);

        // call the update methods of the entity
        verify(company).updateName("Apple");
        verify(company).updateCountry("US");
        verify(company).updateSymbol("AAPL");
        verify(company).updateEmail("test@apple.com");
        verify(company).updateWebsite("https://apple.com");

        // check for unique symbol existsBySymbol("AAPL") and save
        verify(companyRepository).existsBySymbol("AAPL");
        verify(companyRepository).save(company);
    }
    // if website is null service should not call updateWebsite(), but still should save the company
    @Test
    void createCompany_whenWebsiteIsNull_shouldSaveWithoutUpdatingWebsite() {
        // mock company
        Company company = mock(Company.class);
        when(company.getName()).thenReturn("Apple");
        when(company.getCountry()).thenReturn("us");
        when(company.getSymbol()).thenReturn("aapl");
        when(company.getEmail()).thenReturn("test@apple.com");
        when(company.getWebsite()).thenReturn(null);

        when(companyRepository.existsBySymbol("AAPL")).thenReturn(false);
        when(companyRepository.save(company)).thenReturn(company);

        companyService.createCompany(company);

        verify(company, never()).updateWebsite(any());
        verify(companyRepository).save(company);
    }

    // the Service class do not work with the db itself, but delegates it to the repository class
    // getAllCompanies should return what repository returns
    @Test
    void getAllCompanies_shouldReturnRepositoryResult() {
        // list with 2 fake companies
        List<Company> companies = List.of(mock(Company.class), mock(Company.class));
        // tell the repository what to return
        when(companyRepository.findAll()).thenReturn(companies);

        List<Company> result = companyService.getAllCompanies();
        //assertSame(company, saved) checks that the returned object is the same instance
        assertSame(companies, result);
        verify(companyRepository).findAll();
    }

    // when company is not found it should throw an exception
    @Test
    void updateCompany_whenNotFound_shouldThrow() {
        // Repository returns empty
        when(companyRepository.findById(1L)).thenReturn(Optional.empty());

        // create mock company
        Company updated = mock(Company.class);
        // if there is no company with this id - IllegalStateException
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> companyService.updateCompany(1L, updated)
        );
        // message should be "Company with id 1 not found"
        assertEquals("Company with id 1 not found", ex.getMessage());
        verify(companyRepository).findById(1L);
        // save() should not be called
        verify(companyRepository, never()).save(any());
    }

    // if you pass null as updated company it should throw IllegalArgumentException and not to touch the repository at all
    @Test
    void updateCompany_whenBodyIsNull_shouldThrow() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> companyService.updateCompany(1L, null)
        );

        assertEquals("Company body is required", ex.getMessage());
        verifyNoInteractions(companyRepository);
    }

    // when trying to change a symbol to an existing one it should throw an exception
    @Test
    void updateCompany_whenSymbolChangedToExisting_shouldThrow() {
        Company existing = mock(Company.class);
        // there is an “existing” company with symbol "OLD"
        when(existing.getSymbol()).thenReturn("OLD");

        when(companyRepository.findById(5L)).thenReturn(Optional.of(existing));

        // mock company
        Company updated = mock(Company.class);
        when(updated.getName()).thenReturn("New Name");
        when(updated.getCountry()).thenReturn("bg");
        when(updated.getSymbol()).thenReturn("dup");
        when(updated.getEmail()).thenReturn("a@b.com");

        // check if requested symbol already exists
        when(companyRepository.existsBySymbol("DUP")).thenReturn(true);

        // repository should say that "DUP" already exists
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> companyService.updateCompany(5L, updated)
        );
        // Service should throw error with message "Company with symbol DUP already exists"
        assertEquals("Company with symbol DUP already exists", ex.getMessage());
        verify(companyRepository).findById(5L);
        verify(companyRepository).existsBySymbol("DUP");
        verify(companyRepository, never()).save(any());
    }

    // happy ending scenario for updateCompany
    // the company exists, the new data is valid, the symbol can be changed (there is no duplicate)
    // the service normalizes the fields, updates the existing object and saves it
    @Test
    void updateCompany_whenValid_shouldNormalizeAndSave() {
        Company existing = mock(Company.class);
        when(existing.getSymbol()).thenReturn("OLD");
        // existing says that the company that is “already in the database”
        when(companyRepository.findById(10L)).thenReturn(Optional.of(existing));
        // mock company
        Company updated = mock(Company.class);
        when(updated.getName()).thenReturn("  Tesla  ");
        when(updated.getCountry()).thenReturn("  us ");
        when(updated.getSymbol()).thenReturn(" tsla ");
        when(updated.getEmail()).thenReturn("  t@t.com ");
        when(updated.getWebsite()).thenReturn("  https://tesla.com  ");

        when(companyRepository.existsBySymbol("TSLA")).thenReturn(false);
        when(companyRepository.save(existing)).thenReturn(existing);

        Company result = companyService.updateCompany(10L, updated);
        //assertSame(company, saved) checks that the returned object is the same instance.
        assertSame(existing, result);
        // call the update methods of the entity
        verify(existing).updateName("Tesla");
        verify(existing).updateCountry("US");
        verify(existing).updateSymbol("TSLA");
        verify(existing).updateEmail("t@t.com");
        verify(existing).updateWebsite("https://tesla.com");

        verify(companyRepository).save(existing);
    }
}