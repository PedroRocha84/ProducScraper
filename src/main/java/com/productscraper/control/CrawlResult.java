package com.productscraper.control;

import com.productscraper.entity.ProductCard;
import java.util.List;

/**
 * The outcome of crawling one category.
 *
 * @param cards     all cards found, in the order they were read
 * @param pagesRead how many listing pages were read
 * @param truncated true when the crawl stopped because the page limit was reached, so cards may be missing
 */
public record CrawlResult(List<ProductCard> cards, int pagesRead, boolean truncated) {

    /** Keeps an immutable copy so callers cannot change the result afterwards. */
    public CrawlResult {
        cards = List.copyOf(cards);
    }
}
