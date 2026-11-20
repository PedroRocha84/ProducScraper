package com.productscraper.boundary.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.productscraper.TestcontainersConfiguration;
import com.productscraper.entity.Store;
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
class StoreRepositoryTest {

    @Autowired
    private StoreRepository stores;

    @Test
    void savesStoreAndFillsGeneratedFields() {
        Store saved = stores.saveAndFlush(new Store("foxled", "https://foxled.pt", true));
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void findsStoreByName() {
        stores.saveAndFlush(new Store("polegada", "https://polegada.pt", false));
        assertThat(stores.findByName("polegada")).isPresent()
                .get().extracting(Store::isPricesIncludeVat).isEqualTo(false);
    }

    @Test
    void returnsEmptyForUnknownName() {
        assertThat(stores.findByName("nope")).isEmpty();
    }

    @Test
    void rejectsDuplicateName() {
        stores.saveAndFlush(new Store("foxled", "https://foxled.pt", true));
        assertThatThrownBy(() -> stores.saveAndFlush(new Store("foxled", "https://other.pt", true)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
