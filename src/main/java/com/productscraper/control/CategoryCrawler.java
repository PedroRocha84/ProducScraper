package com.productscraper.control;

import com.productscraper.boundary.ListingPage;
import com.productscraper.entity.ProductCard;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Template Method: the crawl loop is the same for every store and lives here in {@link #crawl}.
 * A store only supplies the parts that differ: how to load a page ({@link #loadPage}, where its
 * selectors live) and how to paginate ({@link #paginationStrategy}).
 * <p>
 * The loop protects against two runaway cases: a "next" link that points back to a page already
 * read, and a site that never stops paginating (page limit).
 */
public abstract class CategoryCrawler {

    private final int maxPages;

    /** @param maxPages safety limit of listing pages read per category; must be at least 1 */
    protected CategoryCrawler(int maxPages) {
        if (maxPages < 1) {
            throw new IllegalArgumentException("maxPages must be at least 1");
        }
        this.maxPages = maxPages;
    }

    /** Reads every listing page of a category, starting at its first page, and returns all cards found. */
    public final CrawlResult crawl(String categoryUrl) {
        List<ProductCard> cards = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        PaginationStrategy pagination = paginationStrategy();

        String url = categoryUrl;
        int pagesRead = 0;
        while (url != null) {
            if (pagesRead == maxPages) {
                return new CrawlResult(cards, pagesRead, true);
            }
            visited.add(url);
            ListingPage page = loadPage(url);
            pagesRead++;
            cards.addAll(page.cards());

            // Stop on the last page, or when the next link leads to a page we already read.
            url = pagination.nextPageUrl(url, page)
                    .filter(next -> !visited.contains(next))
                    .orElse(null);
            if (url != null) {
                pauseBeforeNextPage();
            }
        }
        return new CrawlResult(cards, pagesRead, false);
    }

    /** Store-specific step: load the listing page at this URL and expose it as a {@link ListingPage}. */
    protected abstract ListingPage loadPage(String url);

    /** Store-specific step: how this store moves from one listing page to the next. */
    protected abstract PaginationStrategy paginationStrategy();

    /** Hook called between two pages; stores override it to wait (politeness delay). Does nothing by default. */
    protected void pauseBeforeNextPage() {
    }
}
