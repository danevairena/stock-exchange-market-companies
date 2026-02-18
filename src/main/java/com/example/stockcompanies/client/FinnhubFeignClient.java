package com.example.stockcompanies.client;

import com.example.stockcompanies.dto.FinnhubCompanyProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "finnhub", url = "${finnhub.base-url}")
public interface FinnhubFeignClient {

    //getCompanyProfile2 method takes symbol, calls Finnhub and returns DTO object
    @GetMapping("/stock/profile2")
    FinnhubCompanyProfileResponse getCompanyProfile2(
            @RequestParam("symbol") String symbol,
            @RequestParam("token") String token
    );
}