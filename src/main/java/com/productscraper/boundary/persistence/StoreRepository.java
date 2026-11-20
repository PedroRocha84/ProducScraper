package com.productscraper.boundary.persistence;

import com.productscraper.entity.Store;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Database access for {@link Store}.
 */
public interface StoreRepository extends JpaRepository<Store, Long> {

    /** Finds a store by its unique name. */
    Optional<Store> findByName(String name);
}
