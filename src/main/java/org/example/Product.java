package org.example;

import java.math.BigDecimal;

public class Product {

    private final String name;
    private final BigDecimal simplePrice;
    private final BigDecimal discountPrice;
    private final String category;

    public Product(String name, BigDecimal simplePrice, BigDecimal discountPrice, String category) {
        this.name = name;
        this.simplePrice = simplePrice;
        this.discountPrice = discountPrice;
        this.category = category;
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
}
