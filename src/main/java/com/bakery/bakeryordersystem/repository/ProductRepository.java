package com.bakery.bakeryordersystem.repository;

import com.bakery.bakeryordersystem.model.BakeryProduct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProductRepository {

    private final JdbcTemplate jdbc;

    public ProductRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<BakeryProduct> map = (rs, i) -> BakeryProduct.of(
            rs.getString("category"),
            rs.getString("id"),
            rs.getString("name"),
            rs.getDouble("price"),
            rs.getInt("stock"),
            rs.getString("detail"),
            rs.getString("image")
    );

    public List<BakeryProduct> findAll() {
        return jdbc.query("SELECT * FROM products", map);
    }

    public Optional<BakeryProduct> findById(String id) {
        return jdbc.query("SELECT * FROM products WHERE id=?", map, id).stream().findFirst();
    }

    public void save(BakeryProduct b) {
        jdbc.update("INSERT INTO products(id,category,name,price,stock,detail,image) VALUES(?,?,?,?,?,?,?) " +
                        "ON DUPLICATE KEY UPDATE category=VALUES(category), name=VALUES(name), price=VALUES(price), stock=VALUES(stock), detail=VALUES(detail), image=VALUES(image)",
                b.getId(), b.getCategory(), b.getName(), b.getPrice(), b.getStock(), b.getDetail(), b.getImage());
    }

    public void delete(String id) {
        jdbc.update("DELETE FROM products WHERE id=?", id);
    }
}