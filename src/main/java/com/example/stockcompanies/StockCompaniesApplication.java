package com.example.stockcompanies;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class StockCompaniesApplication {

	public static void main(String[] args) {
		// SpringApplication.run starts embedded server (Tomcat)
		//creates all beans: Controller, Service, Repository
		//connects to database
		//makes application ready to accept HTTP requests
		SpringApplication.run(StockCompaniesApplication.class, args);
	}

}
