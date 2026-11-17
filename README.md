# ProductScraper

Scrapes products from several supplier websites, stores them in Postgres per tenant, compares them
across suppliers and produces a single list of unique products.

## Goal

1. Scrape products from different supplier websites.
2. Store everything in Postgres, separated by tenant.
3. Compare products from all suppliers and find the equal ones.
4. Produce a unique product list.

## Requirements

- Java 21
- Maven 3.9+

## Build

```bash
mvn clean package
```

## Status

Step 1: Maven project skeleton. Features are added step by step.
