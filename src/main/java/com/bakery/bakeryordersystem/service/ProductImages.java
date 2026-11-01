package com.bakery.bakeryordersystem.service;
import com.bakery.bakeryordersystem.model.BakeryProduct;
import com.bakery.bakeryordersystem.repository.ProductRepository;
import org.springframework.stereotype.Component;

/**
 * One place that decides which photo a product shows (home page, shop, cart, admin list, customer order/delivery cards).
 * 1) the photo the admin uploaded (/uploads/products/...)  2) the built-in photo matched by product name (/css/images/...)  3) null = emoji.
 * Methods return a ready-to-use URL path, so templates just use th:src="@{${img}}".
 */
@Component("productImages")
public class ProductImages {
    private final ProductRepository repo;
    public ProductImages(ProductRepository repo) { this.repo = repo; }

    /** Built-in photo file name for the demo products (matched by name). */
    public static String forName(String name) {
        if (name == null) return null;
        String n = name.toLowerCase();
        if (n.contains("sliced")) return "sliced_bread.png";
        if (n.contains("cheese")) return "cheese_sandwich.png";
        if (n.contains("chicken")) return "chicken_sandwich.png";
        if (n.contains("dinner")) return "dinner_bun.png";
        if (n.contains("sourdough")) return "sourdough_loaf.png";
        if (n.contains("strawberry")) return "Strawberry_Chocolate.jpeg";
        if (n.contains("pineapple")) return "chocolate_pineapple_gateau.png";
        if (n.contains("marble")) return "Marber_gateau.jpg";
        if (n.contains("coffee")) return "Coffee_choclete_gateau.jpg";
        if (n.contains("rose")) return "Rose_gateau.jpg";
        return null;
    }
    private static String builtIn(String name) { String f = forName(name); return f == null ? null : "/css/images/" + f; }

    /** URL path of a product's photo, or null when it has none. Used from Thymeleaf: ${@productImages.forProduct(p)} */
    public String forProduct(BakeryProduct p) {
        if (p == null) return null;
        if (p.getImage() != null && !p.getImage().isBlank()) return "/uploads/" + ProductService.IMAGE_FOLDER + "/" + p.getImage();
        return builtIn(p.getName());
    }
    /** Same, looked up by product name (order lines only store the name). Used from Thymeleaf: ${@productImages.of(name)} */
    public String of(String name) {
        if (name == null) return null;
        for (BakeryProduct p : repo.findAll())
            if (name.equalsIgnoreCase(p.getName()) && p.getImage() != null && !p.getImage().isBlank()) return forProduct(p);
        return builtIn(name);
    }
}
