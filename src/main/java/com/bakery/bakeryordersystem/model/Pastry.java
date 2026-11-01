package com.bakery.bakeryordersystem.model;
public class Pastry extends BakeryProduct {
    private final String filling;
    public Pastry(String id, String name, double price, int stock, String filling) { super(id, name, price, stock); this.filling = filling; }
    @Override public String getCategory() { return "PASTRY"; }
    @Override public String getDetail() { return filling; }
}
