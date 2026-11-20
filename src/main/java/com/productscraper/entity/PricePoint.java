package com.productscraper.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One row of a product's price history. Rows are only added, never updated or deleted;
 * the latest row is the current price.
 */
@Entity
@Table(name = "price_history")
public class PricePoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** The price exactly as the store displays it (see {@link Store#isPricesIncludeVat()}). */
    @Column(name = "price", nullable = false, precision = 12, scale = 4)
    private BigDecimal price;

    /** ISO currency code, a fixed 3-character column in the database. */
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "currency", nullable = false, length = 3, columnDefinition = "char(3)")
    private String currency;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    /** Required by JPA. */
    protected PricePoint() {
    }

    public PricePoint(Product product, BigDecimal price, String currency, Instant recordedAt) {
        this.product = product;
        this.price = price;
        this.currency = currency;
        this.recordedAt = recordedAt;
    }

    /** Uses the current time just before the insert when no time was given. */
    @PrePersist
    void onCreate() {
        if (recordedAt == null) {
            recordedAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getCurrency() {
        return currency;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }
}
