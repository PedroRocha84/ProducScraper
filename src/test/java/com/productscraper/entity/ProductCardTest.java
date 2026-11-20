package com.productscraper.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ProductCardTest {

    @Test
    void keepsAllValues() {
        ProductCard card = new ProductCard("https://s/p/1", "Lamp", new BigDecimal("9.99"), "https://s/i.jpg");
        assertThat(card.url()).isEqualTo("https://s/p/1");
        assertThat(card.name()).isEqualTo("Lamp");
        assertThat(card.price()).isEqualByComparingTo("9.99");
        assertThat(card.imageUrl()).isEqualTo("https://s/i.jpg");
    }

    @Test
    void allowsMissingPriceAndImage() {
        ProductCard card = new ProductCard("https://s/p/1", "Lamp", null, null);
        assertThat(card.price()).isNull();
        assertThat(card.imageUrl()).isNull();
    }

    @Test
    void rejectsMissingUrl() {
        assertThatThrownBy(() -> new ProductCard(null, "Lamp", null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ProductCard(" ", "Lamp", null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsMissingName() {
        assertThatThrownBy(() -> new ProductCard("https://s/p/1", null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ProductCard("https://s/p/1", "", null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
