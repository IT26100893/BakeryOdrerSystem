package com.bakery.bakeryordersystem.model;
public class CustomCakeBooking {
    public static final double RATE_PER_KG = 3500;
    private final String id, userId, customerName, phone, flavour, message, pickupDate, status;
    private final double sizeKg;
    public CustomCakeBooking(String id, String userId, String customerName, String phone, String flavour, double sizeKg, String message, String pickupDate, String status) {
        this.id = id; this.userId = userId == null ? "" : userId; this.customerName = User.clean(customerName); this.phone = User.clean(phone);
        this.flavour = User.clean(flavour); this.sizeKg = sizeKg; this.message = User.clean(message); this.pickupDate = pickupDate; this.status = status;
    }
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getCustomerName() { return customerName; }
    public String getPhone() { return phone; }
    public String getFlavour() { return flavour; }
    public double getSizeKg() { return sizeKg; }
    public String getMessage() { return message; }
    public String getPickupDate() { return pickupDate; }
    public String getStatus() { return status; }
    public double getEstimatedPrice() { return sizeKg * RATE_PER_KG; }
}
