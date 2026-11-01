package com.bakery.bakeryordersystem.service;
import com.bakery.bakeryordersystem.model.CustomCakeBooking;
import com.bakery.bakeryordersystem.repository.*;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;

@Service
public class CustomCakeService {
    private final CustomCakeRepository repo;
    public CustomCakeService(CustomCakeRepository repo) { this.repo = repo; }

    public List<CustomCakeBooking> search(String q) {
        if (q == null || q.isBlank()) return repo.findAll();
        String k = q.toLowerCase();
        return repo.findAll().stream().filter(b -> b.getCustomerName().toLowerCase().contains(k) || b.getFlavour().toLowerCase().contains(k)).toList();
    }
    public List<CustomCakeBooking> findByUser(String userId) { return repo.findByUserId(userId); }
    public Optional<CustomCakeBooking> find(String id) { return repo.findById(id); }

    /** Admin: full control, including the status. The booking keeps its original owner. */
    public void save(String id, String name, String phone, String flavour, double size, String msg, String date, String status) {
        boolean isNew = id == null || id.isBlank();
        String owner = isNew ? "" : repo.findById(id).map(CustomCakeBooking::getUserId).orElse("");
        repo.save(new CustomCakeBooking(isNew ? FileRepository.newId("BK") : id, owner, name, phone, flavour, size, msg, date, status));
    }
    /** Customer: can only create bookings (always PENDING) and change their own PENDING bookings. */
    public void saveOwn(String userId, String id, String name, String phone, String flavour, double size, String msg, String date) {
        if (id == null || id.isBlank()) {
            if (LocalDate.parse(date).isBefore(LocalDate.now())) throw new IllegalArgumentException("Pickup date cannot be in the past");
            repo.save(new CustomCakeBooking(FileRepository.newId("BK"), userId, name, phone, flavour, size, msg, date, "PENDING"));
        } else {
            CustomCakeBooking old = ownedPending(userId, id);
            repo.save(new CustomCakeBooking(id, userId, name, phone, flavour, size, msg, date, old.getStatus()));
        }
    }
    public void deleteOwn(String userId, String id) { ownedPending(userId, id); repo.delete(id); }
    public Optional<CustomCakeBooking> findOwn(String userId, String id) { return repo.findById(id).filter(b -> b.getUserId().equals(userId)); }
    public void delete(String id) { repo.delete(id); }

    private CustomCakeBooking ownedPending(String userId, String id) {
        CustomCakeBooking b = findOwn(userId, id).orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        if (!"PENDING".equals(b.getStatus())) throw new IllegalArgumentException("Only pending bookings can be changed or cancelled");
        return b;
    }
}
