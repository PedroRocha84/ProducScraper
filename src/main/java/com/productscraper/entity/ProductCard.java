package com.productscraper.entity;

import java.math.BigDecimal;

/**
 * What a store shows about a product on a category listing page (the "card"): enough to create or
 * update the product and its price. The product page itself (description, EAN...) is read later.
 *
 * @param url      absolute URL of the product page; identifies the product inside the store
 * @param name     product name as shown on the card
 * @param price    price as displayed (see Store#isPricesIncludeVat), or null when the card shows none
 * @param imageUrl absolute URL of the card image, or null when there is none
 */
public record ProductCard(String url, String name, BigDecimal price, String imageUrl) {

    /** A card without URL or name cannot identify a product, so it is rejected. */
    public ProductCard {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("A product card needs a url");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("A product card needs a name");
        }
    }
}
