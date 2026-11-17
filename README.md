# ProductScraper

Scrapes products from several store websites, stores them in Postgres per store, compares them
across stores and produces a single list of unique products.

## Goal

1. Scrape products from different store websites by crawling their categories (starting with foxled.pt and polegada.pt).
2. Store everything in Postgres, separated by store, keeping the price history.
3. Compare products from all stores and find the equal ones.
4. Produce a unique product list.

## Requirements

- Java 17
- Maven 3.9+

## Build and test

```bash
mvn clean verify
```

## Run

```bash
mvn spring-boot:run
```

## Status

Step 2: Spring Boot base (the application starts and exits). Features are added step by step.
