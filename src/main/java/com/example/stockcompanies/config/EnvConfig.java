package com.example.stockcompanies.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

// configuration class
@Configuration
public class EnvConfig {
    // @PostConstruct -> execute the method immediately after Spring creates the class
    @PostConstruct
    public void init() {
        // Creates the dotenv loader, that reads the .env file
        Dotenv dotenv = Dotenv.configure().directory("./").ignoreIfMalformed().ignoreIfMissing().load();

        System.setProperty("DB_URL", dotenv.get("DB_URL"));
        System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
        System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));

        System.setProperty("FINNHUB_API_KEY", dotenv.get("FINNHUB_API_KEY"));
        System.setProperty("FINNHUB_BASE_URL", dotenv.get("FINNHUB_BASE_URL"));
    }
}
