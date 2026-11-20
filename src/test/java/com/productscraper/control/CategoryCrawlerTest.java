package com.productscraper.control;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.productscraper.boundary.ListingPage;
import com.productscraper.entity.ProductCard;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Tests the crawl loop with a fake store whose pages are kept in a map. */
class CategoryCrawlerTest {

    /** A store implementation for tests: pages come from a map, and it records what the loop did. */
    private static class FakeStoreCrawler extends CategoryCrawler {
        private final Map<String, ListingPage> pages;
        final List<String> loaded = new ArrayList<>();
        int pauses;

        FakeStoreCrawler(int maxPages, Map<String, ListingPage> pages) {
            super(maxPages);
            this.pages = pages;
        }

        @Override
        protected ListingPage loadPage(String url) {
            loaded.add(url);
            return pages.get(url);
        }

        @Override
        protected PaginationStrategy paginationStrategy() {
            return new NextLinkPagination();
        }

        @Override
        protected void pauseBeforeNextPage() {
            pauses++;
        }
    }

    @Test
    void readsAllPagesInOrderAndCollectsTheCards() {
        FakeStoreCrawler crawler = new FakeStoreCrawler(10, Map.of(
                "u1", FakeListingPage.of("u2", "a", "b"),
                "u2", FakeListingPage.of("u3", "c"),
                "u3", FakeListingPage.of(null, "d")));

        CrawlResult result = crawler.crawl("u1");

        assertThat(crawler.loaded).containsExactly("u1", "u2", "u3");
        assertThat(result.cards()).extracting(ProductCard::name).containsExactly("a", "b", "c", "d");
        assertThat(result.pagesRead()).isEqualTo(3);
        assertThat(result.truncated()).isFalse();
    }

    @Test
    void aSinglePageCategoryReadsOnePage() {
        FakeStoreCrawler crawler = new FakeStoreCrawler(10, Map.of("u1", FakeListingPage.of(null, "a")));

        CrawlResult result = crawler.crawl("u1");

        assertThat(result.pagesRead()).isEqualTo(1);
        assertThat(result.cards()).hasSize(1);
        assertThat(crawler.pauses).isZero();
    }

    @Test
    void anEmptyCategoryReturnsNoCards() {
        FakeStoreCrawler crawler = new FakeStoreCrawler(10, Map.of("u1", FakeListingPage.of(null)));

        CrawlResult result = crawler.crawl("u1");

        assertThat(result.cards()).isEmpty();
        assertThat(result.pagesRead()).isEqualTo(1);
    }

    @Test
    void pausesOnceBetweenEachPairOfPages() {
        FakeStoreCrawler crawler = new FakeStoreCrawler(10, Map.of(
                "u1", FakeListingPage.of("u2", "a"),
                "u2", FakeListingPage.of("u3", "b"),
                "u3", FakeListingPage.of(null, "c")));

        crawler.crawl("u1");

        assertThat(crawler.pauses).isEqualTo(2);
    }

    @Test
    void stopsWhenTheNextLinkPointsBackToAPageAlreadyRead() {
        FakeStoreCrawler crawler = new FakeStoreCrawler(10, Map.of(
                "u1", FakeListingPage.of("u2", "a"),
                "u2", FakeListingPage.of("u1", "b")));

        CrawlResult result = crawler.crawl("u1");

        assertThat(crawler.loaded).containsExactly("u1", "u2");
        assertThat(result.truncated()).isFalse();
    }

    @Test
    void stopsAtThePageLimitAndSaysTheResultIsTruncated() {
        FakeStoreCrawler crawler = new FakeStoreCrawler(2, Map.of(
                "u1", FakeListingPage.of("u2", "a"),
                "u2", FakeListingPage.of("u3", "b"),
                "u3", FakeListingPage.of(null, "c")));

        CrawlResult result = crawler.crawl("u1");

        assertThat(crawler.loaded).containsExactly("u1", "u2");
        assertThat(result.pagesRead()).isEqualTo(2);
        assertThat(result.truncated()).isTrue();
    }

    @Test
    void exactlyAtTheLimitIsNotTruncated() {
        FakeStoreCrawler crawler = new FakeStoreCrawler(2, Map.of(
                "u1", FakeListingPage.of("u2", "a"),
                "u2", FakeListingPage.of(null, "b")));

        CrawlResult result = crawler.crawl("u1");

        assertThat(result.pagesRead()).isEqualTo(2);
        assertThat(result.truncated()).isFalse();
    }

    @Test
    void aLoadFailureStopsTheCrawl() {
        CategoryCrawler crawler = new CategoryCrawler(5) {
            @Override
            protected ListingPage loadPage(String url) {
                throw new IllegalStateException("HTTP 429");
            }

            @Override
            protected PaginationStrategy paginationStrategy() {
                return new NextLinkPagination();
            }
        };

        assertThatThrownBy(() -> crawler.crawl("u1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("HTTP 429");
    }

    @Test
    void rejectsAPageLimitBelowOne() {
        assertThatThrownBy(() -> new FakeStoreCrawler(0, Map.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void theResultCannotBeChangedByTheCaller() {
        FakeStoreCrawler crawler = new FakeStoreCrawler(10, Map.of("u1", FakeListingPage.of(null, "a")));

        CrawlResult result = crawler.crawl("u1");

        assertThatThrownBy(() -> result.cards().clear()).isInstanceOf(UnsupportedOperationException.class);
    }
}
