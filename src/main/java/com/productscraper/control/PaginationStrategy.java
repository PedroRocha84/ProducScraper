package com.productscraper.control;

import com.productscraper.boundary.ListingPage;
import java.util.Optional;

/**
 * Strategy: how to get from one listing page to the next. Stores paginate differently
 * (a "next" link, page numbers in the URL, "load more" buttons), so each way is one implementation.
 */
public interface PaginationStrategy {

    /**
     * @param currentUrl URL of the page that was just read
     * @param page       the page that was just read
     * @return the URL of the next page, or empty when this was the last page
     */
    Optional<String> nextPageUrl(String currentUrl, ListingPage page);
}
