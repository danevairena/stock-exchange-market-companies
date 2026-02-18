package com.example.stockcompanies;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = StockCompaniesApplication.class)
// @ActiveProfiles - use /src/test/resources/application-test.properties
// instead of /src/main/resources/application.properties
@ActiveProfiles("test")
class StockCompaniesApplicationTests {

	@Test
	void contextLoads() {
	}

}
