package com.productscraper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Checks the Flyway migration against a real PostgreSQL: the tables exist and the rules we rely on
 * (unique keys, foreign keys, history rows kept) are enforced by the database itself.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class DatabaseMigrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    /** Every test starts from empty tables. */
    @BeforeEach
    void cleanDatabase() {
        jdbc.execute("TRUNCATE price_history, product_category, product, category, store RESTART IDENTITY CASCADE");
    }

    private long insertStore(String name) {
        return jdbc.queryForObject(
                "INSERT INTO store (name, base_url, prices_include_vat) VALUES (?, ?, ?) RETURNING id",
                Long.class, name, "https://" + name + ".pt", true);
    }

    private long insertCategory(long storeId, String url) {
        return jdbc.queryForObject(
                "INSERT INTO category (store_id, name, url) VALUES (?, ?, ?) RETURNING id",
                Long.class, storeId, "Category", url);
    }

    private long insertProduct(long storeId, String url) {
        return jdbc.queryForObject(
                "INSERT INTO product (store_id, url, name) VALUES (?, ?, ?) RETURNING id",
                Long.class, storeId, url, "Product");
    }

    @Test
    void allCatalogTablesAreCreated() {
        List<String> tables = jdbc.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'", String.class);

        assertThat(tables).contains("store", "category", "product", "product_category", "price_history");
    }

    @Test
    void productUrlIsUniquePerStoreButCanRepeatInAnotherStore() {
        long foxled = insertStore("foxled");
        long polegada = insertStore("polegada");
        insertProduct(foxled, "https://example.pt/p/1");

        // Same URL in another store is allowed
        insertProduct(polegada, "https://example.pt/p/1");

        // Same URL in the same store is rejected
        assertThatThrownBy(() -> insertProduct(foxled, "https://example.pt/p/1"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void productCannotReferenceAnUnknownStore() {
        assertThatThrownBy(() -> insertProduct(999L, "https://example.pt/p/1"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void categoryUrlIsUniquePerStore() {
        long store = insertStore("polegada");
        insertCategory(store, "https://polegada.pt/61-acessorios");

        assertThatThrownBy(() -> insertCategory(store, "https://polegada.pt/61-acessorios"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void productCanBelongToSeveralCategoriesButOnlyOncePerCategory() {
        long store = insertStore("foxled");
        long product = insertProduct(store, "https://foxled.pt/produto/a/");
        long categoryA = insertCategory(store, "https://foxled.pt/produtos/a/");
        long categoryB = insertCategory(store, "https://foxled.pt/produtos/b/");

        jdbc.update("INSERT INTO product_category (product_id, category_id) VALUES (?, ?)", product, categoryA);
        jdbc.update("INSERT INTO product_category (product_id, category_id) VALUES (?, ?)", product, categoryB);

        assertThatThrownBy(() -> jdbc.update(
                "INSERT INTO product_category (product_id, category_id) VALUES (?, ?)", product, categoryA))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void priceHistoryKeepsEveryRowAndTheLatestIsTheCurrentPrice() {
        long store = insertStore("foxled");
        long product = insertProduct(store, "https://foxled.pt/produto/a/");
        String insert = "INSERT INTO price_history (product_id, price, recorded_at) VALUES (?, ?, ?)";
        jdbc.update(insert, product, new BigDecimal("10.00"), Timestamp.from(Instant.parse("2026-01-01T00:00:00Z")));
        jdbc.update(insert, product, new BigDecimal("12.50"), Timestamp.from(Instant.parse("2026-02-01T00:00:00Z")));

        Integer rows = jdbc.queryForObject(
                "SELECT count(*) FROM price_history WHERE product_id = ?", Integer.class, product);
        BigDecimal latest = jdbc.queryForObject(
                "SELECT price FROM price_history WHERE product_id = ? ORDER BY recorded_at DESC LIMIT 1",
                BigDecimal.class, product);

        assertThat(rows).isEqualTo(2);
        assertThat(latest).isEqualByComparingTo("12.50");
    }
}
