package com.bakery.bakeryordersystem.controller;
import com.bakery.bakeryordersystem.model.BakeryProduct;
import com.bakery.bakeryordersystem.model.Review;
import com.bakery.bakeryordersystem.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/** PUBLIC page: full details of one product. Opened by touching a product card on the menu / home page. */
@Controller
public class ProductDetailController {
    private final ProductService products; private final ReviewService reviews;
    public ProductDetailController(ProductService products, ReviewService reviews) { this.products = products; this.reviews = reviews; }

    @GetMapping("/product/{id}") public String page(@PathVariable String id, Model m) {
        BakeryProduct p = products.find(id);                       // 404 if the product does not exist
        List<Review> all = reviews.visibleFor(id);
        m.addAttribute("product", p);
        m.addAttribute("title", p.getName());
        m.addAttribute("summary", reviews.summary(id));
        m.addAttribute("items", all.size() > 3 ? all.subList(0, 3) : all);          // latest few reviews; full list is on /reviews/{id}
        m.addAttribute("related", products.search(null).stream()
                .filter(o -> !o.getId().equals(id) && o.getCategory().equals(p.getCategory())).limit(3).toList());
        m.addAttribute("backUrl", "/product/" + id);               // where "Add To Cart" returns to without JavaScript
        return "product-detail";
    }
}
