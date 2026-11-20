package com.productscraper.boundary.persistence;

import com.productscraper.entity.Category;
import com.productscraper.entity.Store;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Database access for {@link Category}.
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /** Finds a category of a store by its URL. */
    Optional<Category> findByStoreAndUrl(Store store, String url);

    /** All categories of a store. */
    List<Category> findByStore(Store store);
}
