package com.productscraper.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A product of a store. A product is identified by (store, URL). The price is not kept here:
 * it lives in the price history ({@link PricePoint}).
 */
@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "url", nullable = false, length = 1000)
    private String url;

    @Column(name = "name", nullable = false, length = 500)
    private String name;

    /** The store's own reference (for example the "Ref:" shown on foxled.pt). */
    @Column(name = "store_reference", length = 200)
    private String storeReference;

    @Column(name = "brand", length = 200)
    private String brand;

    /** EAN/GTIN, only when the store shows it. */
    @Column(name = "ean", length = 14)
    private String ean;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    /** Only the image URL is kept; images are not downloaded. */
    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "first_seen_at", nullable = false, updatable = false)
    private Instant firstSeenAt;

    @Column(name = "last_seen_at", nullable = false)
    private Instant lastSeenAt;

    /** Empty until the product page has been read (phase 2); products with null here are the work list. */
    @Column(name = "detail_scraped_at")
    private Instant detailScrapedAt;

    @ManyToMany
    @JoinTable(name = "product_category",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<Category> categories = new LinkedHashSet<>();

    /** Required by JPA. */
    protected Product() {
    }

    public Product(Store store, String url, String name) {
        this.store = store;
        this.url = url;
        this.name = name;
    }

    /** Sets the first/last seen times just before the first insert, unless they were already set. */
    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (firstSeenAt == null) {
            firstSeenAt = now;
        }
        if (lastSeenAt == null) {
            lastSeenAt = now;
        }
    }

    /** Adds the product to a category; adding the same category twice has no effect. */
    public void addCategory(Category category) {
        categories.add(category);
    }

    public Long getId() {
        return id;
    }

    public Store getStore() {
        return store;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStoreReference() {
        return storeReference;
    }

    public void setStoreReference(String storeReference) {
        this.storeReference = storeReference;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getEan() {
        return ean;
    }

    public void setEan(String ean) {
        this.ean = ean;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Instant getFirstSeenAt() {
        return firstSeenAt;
    }

    public Instant getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(Instant lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public Instant getDetailScrapedAt() {
        return detailScrapedAt;
    }

    public void setDetailScrapedAt(Instant detailScrapedAt) {
        this.detailScrapedAt = detailScrapedAt;
    }

    public Set<Category> getCategories() {
        return categories;
    }
}
