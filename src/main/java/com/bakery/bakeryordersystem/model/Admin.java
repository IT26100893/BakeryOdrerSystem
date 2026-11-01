package com.bakery.bakeryordersystem.model;
public class Admin extends User {
    public Admin(String id, String name, String email, String phone, String passwordHash) { super(id, name, email, phone, passwordHash); }
    @Override public String getRole() { return "ADMIN"; }
}
