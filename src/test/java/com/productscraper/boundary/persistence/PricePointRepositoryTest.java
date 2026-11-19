package com.productscraper.boundary.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.productscraper.TestcontainersConfiguration;
import com.productscraper.entity.PricePoint;
import com.productscraper.entity.Product;
import com.productscraper.entity.Store;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

/** Runs against a real PostgreSQL (Testcontainers); each test is rolled back automatically. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class PricePointRepositoryTest {

    @Autowired
    private StoreRepository stores;
    @Autowired
    private ProductRepository products;
    @Autowired
    private PricePointRepository prices;
    @Autowired
    private TestEntityManager em;

    private Product product;

    @BeforeEach
    void createProduct() {
        Store store = stores.saveAndFlush(new Store("foxled", "https://foxled.pt", true));
        product = products.saveAndFlush(new Product(store, "https://foxled.pt/p/1", "Lamp"));
    }

    private void add(String price, String when) {
        prices.saveAndFlush(new PricePoint(product, new BigDecimal(price), "EUR", Instant.parse(when)));
    }

    @Test
    void savesPriceWithCurrencyAndScale() {
        add("12.3456", "2026-01-01T00:00:00Z");
        em.clear();
        PricePoint found = prices.findFirstByProductOrderByRecordedAtDesc(product).orElseThrow();
        assertThat(found.getPrice()).isEqualByComparingTo("12.3456");
        assertThat(found.getCurrency()).isEqualTo("EUR");
    }

    @Test
    void latestPriceIsTheMostRecentRow() {
        add("10.00", "2026-01-01T00:00:00Z");
        add("12.00", "2026-03-01T00:00:00Z");
        add("11.00", "2026-02-01T00:00:00Z");
        assertThat(prices.findFirstByProductOrderByRecordedAtDesc(product).orElseThrow().getPrice())
                .isEqualByComparingTo("12.00");
    }

    @Test
    void historyIsReturnedOldestFirst() {
        add("12.00", "2026-03-01T00:00:00Z");
        add("10.00", "2026-01-01T00:00:00Z");
        add("11.00", "2026-02-01T00:00:00Z");
        em.clear(); // read from the database, not from the cache
        assertThat(prices.findByProductOrderByRecordedAtAsc(product))
                .extracting(p -> p.getPrice().toPlainString())
                .containsExactly("10.0000", "11.0000", "12.0000");
    }

    @Test
    void noPriceYieldsEmpty() {
        assertThat(prices.findFirstByProductOrderByRecordedAtDesc(product)).isEmpty();
        assertThat(prices.findByProductOrderByRecordedAtAsc(product)).isEmpty();
    }
}
