package com.productscraper.boundary.persistence;

import com.productscraper.entity.Product;
import com.productscraper.entity.Store;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Database access for {@link Product}.
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    /** Finds a product of a store by its page URL (the product's identity). */
    Optional<Product> findByStoreAndUrl(Store store, String url);

    /** The phase 2 work list: products of a store whose product page has not been read yet. */
    List<Product> findByStoreAndDetailScrapedAtIsNull(Store store);
}
