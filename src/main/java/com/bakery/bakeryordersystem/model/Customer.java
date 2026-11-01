package com.bakery.bakeryordersystem.model;
public class Customer extends User {
    public Customer(String id, String name, String email, String phone, String passwordHash) { super(id, name, email, phone, passwordHash); }
    @Override public String getRole() { return "CUSTOMER"; }
}
