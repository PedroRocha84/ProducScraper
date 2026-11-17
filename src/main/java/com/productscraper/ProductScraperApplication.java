package com.productscraper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the ProductScraper batch job.
 * <p>
 * For now the application only starts the Spring context and exits. The crawling logic is added
 * in later steps, organised in the Boundary-Control-Entity packages.
 */
@SpringBootApplication
public class ProductScraperApplication {

    /** Starts the Spring Boot application. */
    public static void main(String[] args) {
        SpringApplication.run(ProductScraperApplication.class, args);
    }
}
