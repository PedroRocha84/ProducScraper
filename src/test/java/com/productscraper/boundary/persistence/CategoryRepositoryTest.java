package com.productscraper.boundary.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.productscraper.TestcontainersConfiguration;
import com.productscraper.entity.Category;
import com.productscraper.entity.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

/** Runs against a real PostgreSQL (Testcontainers); each test is rolled back automatically. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class CategoryRepositoryTest {

    @Autowired
    private StoreRepository stores;
    @Autowired
    private CategoryRepository categories;

    private Store store;

    @BeforeEach
    void createStore() {
        store = stores.saveAndFlush(new Store("polegada", "https://polegada.pt", false));
    }

    @Test
    void findsCategoryByStoreAndUrl() {
        categories.saveAndFlush(new Category(store, "Lamps", "https://polegada.pt/10-lamps"));
        assertThat(categories.findByStoreAndUrl(store, "https://polegada.pt/10-lamps")).isPresent();
        assertThat(categories.findByStoreAndUrl(store, "https://polegada.pt/other")).isEmpty();
    }

    @Test
    void listsOnlyCategoriesOfTheGivenStore() {
        Store other = stores.saveAndFlush(new Store("foxled", "https://foxled.pt", true));
        categories.saveAndFlush(new Category(store, "Lamps", "https://polegada.pt/10-lamps"));
        categories.saveAndFlush(new Category(other, "LED", "https://foxled.pt/led/"));
        assertThat(categories.findByStore(store)).extracting(Category::getName).containsExactly("Lamps");
    }

    @Test
    void allowsSameNameWithDifferentUrls() {
        categories.saveAndFlush(new Category(store, "Lamps", "https://polegada.pt/10-lamps"));
        categories.saveAndFlush(new Category(store, "Lamps", "https://polegada.pt/20-lamps"));
        assertThat(categories.findByStore(store)).hasSize(2);
    }

    @Test
    void rejectsDuplicateUrlInSameStore() {
        categories.saveAndFlush(new Category(store, "Lamps", "https://polegada.pt/10-lamps"));
        assertThatThrownBy(() -> categories.saveAndFlush(new Category(store, "Again", "https://polegada.pt/10-lamps")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
