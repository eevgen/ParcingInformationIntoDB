package org.example;

import java.math.BigDecimal;

public class Product {

    private final String name;
    private final BigDecimal simplePrice;
    private final BigDecimal discountPrice;
    private final String category;
    private final String link;
    private final String supermarket;

    public Product(String name, BigDecimal simplePrice, BigDecimal discountPrice, String category, String link, String supermarket) {
        this.name = name;
        this.simplePrice = simplePrice;
        this.discountPrice = discountPrice;
        this.category = category;
        this.link = link;
        this.supermarket = supermarket;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getSimplePrice() {
        return simplePrice;
    }

    public BigDecimal getDiscountPrice() {
        return discountPrice;
    }

    public String getCategory() {
        return category;
    }
    public String getLink() {
        return link;
    }

    public String getSupermarket() {
        return supermarket;
    }
}
