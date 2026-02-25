package com.example.stockcompanies.mapper;

import com.example.stockcompanies.dto.CompanyStocksResponse;
import com.example.stockcompanies.dto.FinnhubCompanyProfileResponse;
import com.example.stockcompanies.model.Company;
import com.example.stockcompanies.model.CompanyStock;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface CompanyStocksMapper {

    // base mapping от db entity -> DTO
    @Mapping(target = "marketCapitalization", ignore = true)
    @Mapping(target = "shareOutstanding", ignore = true)
    CompanyStocksResponse fromCompany(Company company);

    // create a new DTO from stock
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "country", ignore = true)
    @Mapping(target = "symbol", ignore = true)
    @Mapping(target = "website", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    CompanyStocksResponse fromStock(CompanyStock stock);

    // create a new DTO from Finnhub
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "country", ignore = true)
    @Mapping(target = "symbol", ignore = true)
    @Mapping(target = "website", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    CompanyStocksResponse fromFinnhub(FinnhubCompanyProfileResponse finnhub);

    // convenience methods
    default CompanyStocksResponse toResponse(Company company, CompanyStock stock) {
        CompanyStocksResponse base = fromCompany(company);
        if (stock == null) return base;

        CompanyStocksResponse s = fromStock(stock);
        return CompanyStocksResponse.builder()
                .id(base.getId())
                .name(base.getName())
                .country(base.getCountry())
                .symbol(base.getSymbol())
                .website(base.getWebsite())
                .email(base.getEmail())
                .createdAt(base.getCreatedAt())
                .marketCapitalization(s.getMarketCapitalization())
                .shareOutstanding(s.getShareOutstanding())
                .build();
    }

    default CompanyStocksResponse toResponse(Company company, FinnhubCompanyProfileResponse finnhub) {
        CompanyStocksResponse base = fromCompany(company);
        if (finnhub == null) return base;

        CompanyStocksResponse f = fromFinnhub(finnhub);
        return CompanyStocksResponse.builder()
                .id(base.getId())
                .name(base.getName())
                .country(base.getCountry())
                .symbol(base.getSymbol())
                .website(base.getWebsite())
                .email(base.getEmail())
                .createdAt(base.getCreatedAt())
                .marketCapitalization(f.getMarketCapitalization())
                .shareOutstanding(f.getShareOutstanding())
                .build();
    }
}
