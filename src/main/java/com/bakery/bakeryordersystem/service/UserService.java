package com.bakery.bakeryordersystem.service;
import com.bakery.bakeryordersystem.exception.UserNotFoundException;
import com.bakery.bakeryordersystem.model.User;
import com.bakery.bakeryordersystem.repository.*;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class UserService {
    private final UserRepository repo;
    public UserService(UserRepository repo) { this.repo = repo; }

    public List<User> search(String q) {
        if (q == null || q.isBlank()) return repo.findAll();
        String k = q.toLowerCase();
        return repo.findAll().stream().filter(u -> u.getName().toLowerCase().contains(k) || u.getEmail().toLowerCase().contains(k)).toList();
    }
    public User find(String id) { return repo.findById(id).orElseThrow(() -> new UserNotFoundException(id)); }

    /** Admin create/update. A blank password on update keeps the old password. */
    public void save(String id, String role, String name, String email, String phone, String password) {
        boolean isNew = id == null || id.isBlank();
        String uid = isNew ? FileRepository.newId("U") : id;
        String hash;
        if (password == null || password.isBlank()) {
            if (isNew) throw new IllegalArgumentException("A password is required for a new user");
            hash = find(uid).getPasswordHash();
        } else { checkPassword(password); hash = PasswordHasher.hash(password); }
        checkEmailFree(uid, email);
        repo.save(User.of(role, uid, name, email, phone, hash));
    }
    /** Public sign-up: the role is always CUSTOMER, whatever the browser sends. */
    public User register(String name, String email, String phone, String password) {
        checkPassword(password);
        String uid = FileRepository.newId("U");
        checkEmailFree(uid, email);
        User u = User.of("CUSTOMER", uid, name, email, phone, PasswordHasher.hash(password));
        repo.save(u);
        return u;
    }
    public Optional<User> authenticate(String email, String password) {
        if (email == null || password == null) return Optional.empty();
        return repo.findByEmail(email.trim()).filter(u -> PasswordHasher.matches(password, u.getPasswordHash()));
    }
    /** Customers editing their own profile: the role can never change here. */
    public User updateProfile(String id, String name, String email, String phone, String newPassword) {
        User old = find(id);
        checkEmailFree(id, email);
        String hash = old.getPasswordHash();
        if (newPassword != null && !newPassword.isBlank()) { checkPassword(newPassword); hash = PasswordHasher.hash(newPassword); }
        User u = User.of(old.getRole(), id, name, email, phone, hash);
        repo.save(u);
        return u;
    }
    public void delete(String id) { repo.delete(id); }

    /** Used at start-up to make sure the demo accounts can log in. */
    public void seedAccount(String email, String role, String name, String phone, String password) {
        Optional<User> existing = repo.findByEmail(email);
        if (existing.isEmpty()) repo.save(User.of(role, FileRepository.newId("U"), name, email, phone, PasswordHasher.hash(password)));
        else if (existing.get().getPasswordHash().isBlank()) {
            User u = existing.get();
            repo.save(User.of(u.getRole(), u.getId(), u.getName(), u.getEmail(), u.getPhone(), PasswordHasher.hash(password)));
        }
    }
    private void checkPassword(String p) { if (p == null || p.length() < 6) throw new IllegalArgumentException("Password must be at least 6 characters"); }
    private void checkEmailFree(String uid, String email) {
        repo.findByEmail(email.trim()).filter(u -> !u.getId().equals(uid)).ifPresent(u -> { throw new IllegalArgumentException("That email is already registered"); });
    }
}
