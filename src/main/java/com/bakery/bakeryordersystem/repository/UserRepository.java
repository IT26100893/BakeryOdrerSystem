package com.bakery.bakeryordersystem.repository;
import com.bakery.bakeryordersystem.model.User;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbc;
    public UserRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    private final RowMapper<User> map = (rs, i) -> User.of(rs.getString("role"), rs.getString("id"),
            rs.getString("name"), rs.getString("email"), rs.getString("phone"), rs.getString("password_hash"));

    public List<User> findAll() { return jdbc.query("SELECT * FROM users", map); }
    public Optional<User> findById(String id) { return jdbc.query("SELECT * FROM users WHERE id=?", map, id).stream().findFirst(); }
    public Optional<User> findByEmail(String email) { return jdbc.query("SELECT * FROM users WHERE LOWER(email)=LOWER(?)", map, email).stream().findFirst(); }
    public void save(User u) {
        jdbc.update("INSERT INTO users(id,role,name,email,phone,password_hash) VALUES(?,?,?,?,?,?) " +
                        "ON DUPLICATE KEY UPDATE role=VALUES(role), name=VALUES(name), email=VALUES(email), phone=VALUES(phone), password_hash=VALUES(password_hash)",
                u.getId(), u.getRole(), u.getName(), u.getEmail(), u.getPhone(), u.getPasswordHash());
    }
    public void delete(String id) { jdbc.update("DELETE FROM users WHERE id=?", id); }
}
