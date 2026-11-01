package com.bakery.bakeryordersystem.repository;
import com.bakery.bakeryordersystem.model.CustomCakeBooking;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public class CustomCakeRepository {
    private final JdbcTemplate jdbc;
    public CustomCakeRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    private final RowMapper<CustomCakeBooking> map = (rs, i) -> new CustomCakeBooking(rs.getString("id"), rs.getString("user_id"),
            rs.getString("customer_name"), rs.getString("phone"), rs.getString("flavour"), rs.getDouble("size_kg"),
            rs.getString("message"), rs.getString("pickup_date"), rs.getString("status"));

    public List<CustomCakeBooking> findAll() { return jdbc.query("SELECT * FROM bookings", map); }
    public List<CustomCakeBooking> findByUserId(String uid) { return jdbc.query("SELECT * FROM bookings WHERE user_id=?", map, uid); }
    public Optional<CustomCakeBooking> findById(String id) { return jdbc.query("SELECT * FROM bookings WHERE id=?", map, id).stream().findFirst(); }
    public void save(CustomCakeBooking b) {
        jdbc.update("INSERT INTO bookings(id,user_id,customer_name,phone,flavour,size_kg,message,pickup_date,status) VALUES(?,?,?,?,?,?,?,?,?) " +
                        "ON DUPLICATE KEY UPDATE user_id=VALUES(user_id), customer_name=VALUES(customer_name), phone=VALUES(phone), flavour=VALUES(flavour), " +
                        "size_kg=VALUES(size_kg), message=VALUES(message), pickup_date=VALUES(pickup_date), status=VALUES(status)",
                b.getId(), b.getUserId(), b.getCustomerName(), b.getPhone(), b.getFlavour(), b.getSizeKg(), b.getMessage(), b.getPickupDate(), b.getStatus());
    }
    public void delete(String id) { jdbc.update("DELETE FROM bookings WHERE id=?", id); }
}
