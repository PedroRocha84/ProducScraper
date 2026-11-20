package com.productscraper.control;

import com.productscraper.boundary.ListingPage;
import java.util.Optional;

/**
 * Follows the "next page" link of the page. Works for stores that show a next link until the last page.
 */
public class NextLinkPagination implements PaginationStrategy {

    @Override
    public Optional<String> nextPageUrl(String currentUrl, ListingPage page) {
        return page.nextLinkUrl().filter(url -> !url.isBlank());
    }
}
