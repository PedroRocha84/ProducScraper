package com.productscraper.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * A store (website) whose products are collected, for example foxled.pt.
 */
@Entity
@Table(name = "store")
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "base_url", nullable = false, length = 500)
    private String baseUrl;

    /** Prices are stored as the store displays them; this says whether they include VAT. */
    @Column(name = "prices_include_vat", nullable = false)
    private boolean pricesIncludeVat;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Required by JPA. */
    protected Store() {
    }

    public Store(String name, String baseUrl, boolean pricesIncludeVat) {
        this.name = name;
        this.baseUrl = baseUrl;
        this.pricesIncludeVat = pricesIncludeVat;
    }

    /** Sets the creation time just before the first insert. */
    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public boolean isPricesIncludeVat() {
        return pricesIncludeVat;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
