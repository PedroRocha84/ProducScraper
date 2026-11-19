package com.productscraper.boundary.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.productscraper.TestcontainersConfiguration;
import com.productscraper.entity.Category;
import com.productscraper.entity.Product;
import com.productscraper.entity.Store;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

/** Runs against a real PostgreSQL (Testcontainers); each test is rolled back automatically. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class ProductRepositoryTest {

    @Autowired
    private StoreRepository stores;
    @Autowired
    private CategoryRepository categories;
    @Autowired
    private ProductRepository products;
    @Autowired
    private TestEntityManager em;

    private Store store;

    @BeforeEach
    void createStore() {
        store = stores.saveAndFlush(new Store("foxled", "https://foxled.pt", true));
    }

    @Test
    void savesProductWithAllFields() {
        Product product = new Product(store, "https://foxled.pt/p/1", "Lamp");
        product.setBrand("Maxled");
        product.setEan("1234567890123");
        product.setStoreReference("REF1");
        product.setImageUrl("https://img/1.jpg");
        product.setDescription("A long description");
        products.saveAndFlush(product);
        em.clear(); // forces the next read to come from the database, not the cache

        Product found = products.findByStoreAndUrl(store, "https://foxled.pt/p/1").orElseThrow();
        assertThat(found.getBrand()).isEqualTo("Maxled");
        assertThat(found.getEan()).isEqualTo("1234567890123");
        assertThat(found.getDescription()).isEqualTo("A long description");
        assertThat(found.getFirstSeenAt()).isNotNull();
        assertThat(found.getLastSeenAt()).isNotNull();
        assertThat(found.getDetailScrapedAt()).isNull();
    }

    @Test
    void rejectsDuplicateUrlInSameStore() {
        products.saveAndFlush(new Product(store, "https://foxled.pt/p/1", "Lamp"));
        assertThatThrownBy(() -> products.saveAndFlush(new Product(store, "https://foxled.pt/p/1", "Copy")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void allowsSameUrlInDifferentStores() {
        Store other = stores.saveAndFlush(new Store("polegada", "https://polegada.pt", false));
        products.saveAndFlush(new Product(store, "https://x/p/1", "Lamp"));
        products.saveAndFlush(new Product(other, "https://x/p/1", "Lamp"));
        assertThat(products.count()).isEqualTo(2);
    }

    @Test
    void findsOnlyProductsWithoutDetails() {
        Product pending = new Product(store, "https://foxled.pt/p/1", "Pending");
        Product done = new Product(store, "https://foxled.pt/p/2", "Done");
        done.setDetailScrapedAt(Instant.now());
        products.saveAndFlush(pending);
        products.saveAndFlush(done);

        assertThat(products.findByStoreAndDetailScrapedAtIsNull(store))
                .extracting(Product::getName).containsExactly("Pending");
    }

    @Test
    void workListIgnoresOtherStores() {
        Store other = stores.saveAndFlush(new Store("polegada", "https://polegada.pt", false));
        products.saveAndFlush(new Product(other, "https://polegada.pt/p/1", "Elsewhere"));
        assertThat(products.findByStoreAndDetailScrapedAtIsNull(store)).isEmpty();
    }

    @Test
    void savesProductCategoryLinks() {
        Category category = categories.saveAndFlush(new Category(store, "LED", "https://foxled.pt/led/"));
        Product product = new Product(store, "https://foxled.pt/p/1", "Lamp");
        product.addCategory(category);
        products.saveAndFlush(product);
        em.clear();

        Product found = products.findByStoreAndUrl(store, "https://foxled.pt/p/1").orElseThrow();
        assertThat(found.getCategories()).extracting(Category::getUrl).containsExactly("https://foxled.pt/led/");
    }
}
