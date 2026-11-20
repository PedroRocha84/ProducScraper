package com.productscraper.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

/**
 * Plain unit tests (no Spring, no database) for the logic inside the entities.
 */
class EntityLifecycleTest {

    private final Store store = new Store("foxled", "https://foxled.pt", true);

    @Test
    void storeKeepsConstructorValues() {
        assertThat(store.getName()).isEqualTo("foxled");
        assertThat(store.getBaseUrl()).isEqualTo("https://foxled.pt");
        assertThat(store.isPricesIncludeVat()).isTrue();
        assertThat(store.getId()).isNull();
    }

    @Test
    void storeSetsCreatedAtOnCreate() {
        store.onCreate();
        assertThat(store.getCreatedAt()).isNotNull();
    }

    @Test
    void storeKeepsExistingCreatedAt() {
        store.onCreate();
        Instant first = store.getCreatedAt();
        store.onCreate();
        assertThat(store.getCreatedAt()).isEqualTo(first);
    }

    @Test
    void categorySetsCreatedAtOnCreate() {
        Category category = new Category(store, "LED", "https://foxled.pt/produtos/led/");
        assertThat(category.getStore()).isSameAs(store);
        assertThat(category.getName()).isEqualTo("LED");
        assertThat(category.getUrl()).isEqualTo("https://foxled.pt/produtos/led/");
        category.onCreate();
        assertThat(category.getCreatedAt()).isNotNull();
    }

    @Test
    void productSetsSeenTimesOnCreate() {
        Product product = new Product(store, "https://foxled.pt/p/1", "Lamp");
        product.onCreate();
        assertThat(product.getFirstSeenAt()).isNotNull();
        assertThat(product.getLastSeenAt()).isEqualTo(product.getFirstSeenAt());
    }

    @Test
    void productKeepsLastSeenAtWhenAlreadySet() {
        Product product = new Product(store, "https://foxled.pt/p/1", "Lamp");
        Instant seen = Instant.parse("2026-01-01T00:00:00Z");
        product.setLastSeenAt(seen);
        product.onCreate();
        assertThat(product.getLastSeenAt()).isEqualTo(seen);
        assertThat(product.getFirstSeenAt()).isNotNull();
    }

    @Test
    void productAddCategoryIgnoresDuplicates() {
        Product product = new Product(store, "https://foxled.pt/p/1", "Lamp");
        Category category = new Category(store, "LED", "https://foxled.pt/produtos/led/");
        product.addCategory(category);
        product.addCategory(category);
        assertThat(product.getCategories()).containsExactly(category);
    }

    @Test
    void productSettersChangeValues() {
        Product product = new Product(store, "https://foxled.pt/p/1", "Lamp");
        product.setName("Lamp 2");
        product.setBrand("Maxled");
        product.setEan("1234567890123");
        product.setStoreReference("REF1");
        product.setDescription("Text");
        product.setImageUrl("https://img/1.jpg");
        Instant scraped = Instant.parse("2026-02-01T00:00:00Z");
        product.setDetailScrapedAt(scraped);
        assertThat(product.getName()).isEqualTo("Lamp 2");
        assertThat(product.getBrand()).isEqualTo("Maxled");
        assertThat(product.getEan()).isEqualTo("1234567890123");
        assertThat(product.getStoreReference()).isEqualTo("REF1");
        assertThat(product.getDescription()).isEqualTo("Text");
        assertThat(product.getImageUrl()).isEqualTo("https://img/1.jpg");
        assertThat(product.getDetailScrapedAt()).isEqualTo(scraped);
    }

    @Test
    void pricePointUsesNowWhenNoTimeGiven() {
        Product product = new Product(store, "https://foxled.pt/p/1", "Lamp");
        PricePoint point = new PricePoint(product, new BigDecimal("9.99"), "EUR", null);
        point.onCreate();
        assertThat(point.getRecordedAt()).isNotNull();
        assertThat(point.getPrice()).isEqualByComparingTo("9.99");
        assertThat(point.getCurrency()).isEqualTo("EUR");
        assertThat(point.getProduct()).isSameAs(product);
    }

    @Test
    void pricePointKeepsGivenTime() {
        Product product = new Product(store, "https://foxled.pt/p/1", "Lamp");
        Instant at = Instant.parse("2026-03-01T10:00:00Z");
        PricePoint point = new PricePoint(product, BigDecimal.ONE, "EUR", at);
        point.onCreate();
        assertThat(point.getRecordedAt()).isEqualTo(at);
    }
}
