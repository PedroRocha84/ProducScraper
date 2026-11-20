package com.productscraper.boundary;

import com.productscraper.entity.ProductCard;
import java.util.List;
import java.util.Optional;

/**
 * One loaded category listing page. This is the boundary to the store's website: the Playwright
 * implementation (a later step) holds the per-store selectors, tests use in-memory fakes.
 * All URLs returned must be absolute.
 */
public interface ListingPage {

    /** The product cards shown on this page, in page order. */
    List<ProductCard> cards();

    /** Absolute URL of the "next page" link, or empty when the page has no such link. */
    Optional<String> nextLinkUrl();
}
