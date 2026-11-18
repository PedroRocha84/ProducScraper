-- Flyway migration V1: the catalog tables.
-- Every product belongs to a store, so all stores share the same tables and can be compared later.

-- A store (website) whose products we collect
CREATE TABLE store (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name               VARCHAR(100) NOT NULL UNIQUE,
    base_url           VARCHAR(500) NOT NULL,
    -- Prices are stored exactly as the store displays them; this flag says whether they include VAT
    prices_include_vat BOOLEAN      NOT NULL,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- A category page of a store. Identified by URL because names repeat (e.g. "Acessórios" appears many times)
CREATE TABLE category (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    store_id   BIGINT        NOT NULL REFERENCES store (id),
    name       VARCHAR(300)  NOT NULL,
    url        VARCHAR(1000) NOT NULL,
    created_at TIMESTAMPTZ   NOT NULL DEFAULT now(),
    UNIQUE (store_id, url)
);

-- A product of a store. A product is identified by (store, product URL)
CREATE TABLE product (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    store_id          BIGINT        NOT NULL REFERENCES store (id),
    url               VARCHAR(1000) NOT NULL,
    name              VARCHAR(500)  NOT NULL,
    -- The store's own reference (e.g. the "Ref:" shown on foxled.pt)
    store_reference   VARCHAR(200),
    brand             VARCHAR(200),
    -- EAN/GTIN, only when the store shows it (usually on the product page)
    ean               VARCHAR(14),
    description       TEXT,
    -- We only keep the image URL, we do not download images
    image_url         VARCHAR(1000),
    first_seen_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    last_seen_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    -- Empty until the product page has been read (phase 2); products with NULL here are the work list
    detail_scraped_at TIMESTAMPTZ,
    UNIQUE (store_id, url)
);

-- Partial indexes: small, and only contain the rows we look for
CREATE INDEX idx_product_ean ON product (ean) WHERE ean IS NOT NULL;
CREATE INDEX idx_product_detail_pending ON product (store_id) WHERE detail_scraped_at IS NULL;

-- A product can appear in several categories
CREATE TABLE product_category (
    product_id  BIGINT NOT NULL REFERENCES product (id),
    category_id BIGINT NOT NULL REFERENCES category (id),
    PRIMARY KEY (product_id, category_id)
);

CREATE INDEX idx_product_category_category ON product_category (category_id);

-- Price history: a row is added only when the price changes (and the first time a product is seen).
-- The latest row is the current price; rows are never updated or deleted.
CREATE TABLE price_history (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    product_id  BIGINT         NOT NULL REFERENCES product (id),
    price       NUMERIC(12, 4) NOT NULL,
    currency    CHAR(3)        NOT NULL DEFAULT 'EUR',
    recorded_at TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE INDEX idx_price_history_product_time ON price_history (product_id, recorded_at DESC);
