package com.example.stockcompanies;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = StockCompaniesApplication.class)
@ActiveProfiles("test")
class StockCompaniesApplicationTests {

	@Test
	void contextLoads() {
	}

}
