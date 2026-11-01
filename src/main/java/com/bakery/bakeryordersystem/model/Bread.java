package com.bakery.bakeryordersystem.model;
public class Bread extends BakeryProduct {
    private final String type;
    public Bread(String id, String name, double price, int stock, String type) { super(id, name, price, stock); this.type = type; }
    @Override public String getCategory() { return "BREAD"; }
    @Override public String getDetail() { return type; }
}
