package com.bakery.bakeryordersystem.model;
/** Abstraction + encapsulation: base class for all system users. The password is only ever stored as a salted hash. */
public abstract class User {
    private final String id, name, email, phone, passwordHash;
    protected User(String id, String name, String email, String phone, String passwordHash) {
        this.id = id; this.name = clean(name); this.email = clean(email); this.phone = clean(phone);
        this.passwordHash = passwordHash == null ? "" : passwordHash;
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getPasswordHash() { return passwordHash; }
    public abstract String getRole();
    public static String clean(String s) { return s == null ? "" : s.replace("|", "/").replace("\n", " ").trim(); }
    public static User of(String role, String id, String n, String e, String p, String hash) {
        return "ADMIN".equals(role) ? new Admin(id, n, e, p, hash) : new Customer(id, n, e, p, hash);
    }
}
