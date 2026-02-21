package com.example.stockcompanies.mapper;

import com.example.stockcompanies.dto.CompanyStocksResponse;
import com.example.stockcompanies.dto.FinnhubCompanyProfileResponse;
import com.example.stockcompanies.model.Company;
import com.example.stockcompanies.model.CompanyStock;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = MapStructConfig.class)
public interface CompanyStocksMapper {

    // base mapping от db entity -> DTO
    @Mapping(target = "marketCapitalization", ignore = true)
    @Mapping(target = "shareOutstanding", ignore = true)
    CompanyStocksResponse fromCompany(Company company);

    // enrichment mapping
    @BeanMapping(ignoreByDefault = true, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "marketCapitalization", source = "marketCapitalization")
    @Mapping(target = "shareOutstanding", source = "shareOutstanding")
    void enrichFromStock(CompanyStock stock, @MappingTarget CompanyStocksResponse target);

    // enrichment mapping Finnhub -> existing DTO
    @BeanMapping(ignoreByDefault = true, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "marketCapitalization", source = "marketCapitalization")
    @Mapping(target = "shareOutstanding", source = "shareOutstanding")
    void enrichFromFinnhub(FinnhubCompanyProfileResponse finnhub,
                           @MappingTarget CompanyStocksResponse target);

    // convenience methods
    default CompanyStocksResponse toResponse(Company company, CompanyStock stock) {
        CompanyStocksResponse dto = fromCompany(company);
        if (stock != null) {
            enrichFromStock(stock, dto);
        }
        return dto;
    }

    default CompanyStocksResponse toResponse(Company company, FinnhubCompanyProfileResponse finnhub) {
        CompanyStocksResponse dto = fromCompany(company);
        if (finnhub != null) {
            enrichFromFinnhub(finnhub, dto);
        }
        return dto;
    }
}
