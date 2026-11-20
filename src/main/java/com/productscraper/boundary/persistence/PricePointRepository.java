package com.productscraper.boundary.persistence;

import com.productscraper.entity.PricePoint;
import com.productscraper.entity.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Database access for {@link PricePoint}.
 */
public interface PricePointRepository extends JpaRepository<PricePoint, Long> {

    /** The latest price of a product, which is its current price. */
    Optional<PricePoint> findFirstByProductOrderByRecordedAtDesc(Product product);

    /** The full price history of a product, oldest first. */
    List<PricePoint> findByProductOrderByRecordedAtAsc(Product product);
}
