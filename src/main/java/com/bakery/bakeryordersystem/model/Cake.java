package com.bakery.bakeryordersystem.model;
public class Cake extends BakeryProduct {
    private final String flavour;
    public Cake(String id, String name, double price, int stock, String flavour) { super(id, name, price, stock); this.flavour = flavour; }
    @Override public String getCategory() { return "CAKE"; }
    @Override public String getDetail() { return flavour; }
}
