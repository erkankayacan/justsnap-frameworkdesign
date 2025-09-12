package com.example.db;

public class Product {
    private final long id;
    private final String sku;
    private final String name;
    private final double price;
    private final String category;

    public Product(long id, String sku, String name, double price, String category) {
        this.id = id; this.sku = sku; this.name = name; this.price = price; this.category = category;
    }

    public long getId() { return id; }
    public String getSku() { return sku; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getCategory() { return category; }
}
