package com.productscraper.control;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NextLinkPaginationTest {

    private final NextLinkPagination pagination = new NextLinkPagination();

    @Test
    void returnsTheNextLink() {
        FakeListingPage page = FakeListingPage.of("https://s/page/2", "a");
        assertThat(pagination.nextPageUrl("https://s/page/1", page)).contains("https://s/page/2");
    }

    @Test
    void returnsEmptyOnTheLastPage() {
        FakeListingPage page = FakeListingPage.of(null, "a");
        assertThat(pagination.nextPageUrl("https://s/page/2", page)).isEmpty();
    }

    @Test
    void treatsABlankLinkAsNoLink() {
        FakeListingPage page = FakeListingPage.of("  ", "a");
        assertThat(pagination.nextPageUrl("https://s/page/2", page)).isEmpty();
    }
}
