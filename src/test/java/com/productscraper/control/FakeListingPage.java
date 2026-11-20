package com.productscraper.control;

import com.productscraper.boundary.ListingPage;
import com.productscraper.entity.ProductCard;
import java.util.List;
import java.util.Optional;

/** In-memory listing page used by the tests instead of a real browser. */
record FakeListingPage(List<ProductCard> cards, String next) implements ListingPage {

    @Override
    public Optional<String> nextLinkUrl() {
        return Optional.ofNullable(next);
    }

    /** A page with the given product names (URLs are derived from the names) and an optional next link. */
    static FakeListingPage of(String next, String... names) {
        List<ProductCard> cards = java.util.Arrays.stream(names)
                .map(n -> new ProductCard("https://s/p/" + n, n, null, null))
                .toList();
        return new FakeListingPage(cards, next);
    }
}
