package com.productscraper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

/**
 * Smoke test: proves that the Spring context starts (including the database connection and the Flyway
 * migrations), which also covers {@link ProductScraperApplication#main(String[])} wiring.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ProductScraperApplicationTests {

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoads() {
        assertThat(context).isNotNull();
    }

    @Test
    void applicationNameComesFromConfiguration() {
        assertThat(context.getEnvironment().getProperty("spring.application.name")).isEqualTo("product-scraper");
    }
}
