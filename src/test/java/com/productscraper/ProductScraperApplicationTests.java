package com.productscraper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

/**
 * Smoke tests: the whole application context starts against a throw-away PostgreSQL (Testcontainers).
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ProductScraperApplicationTests {

    @Autowired
    private Environment environment;

    /** Fails if any bean, Flyway migration or JPA mapping cannot start. */
    @Test
    void contextLoads() {
    }

    /** The application name comes from application.yml. */
    @Test
    void applicationNameComesFromConfiguration() {
        assertThat(environment.getProperty("spring.application.name")).isEqualTo("product-scraper");
    }
}
