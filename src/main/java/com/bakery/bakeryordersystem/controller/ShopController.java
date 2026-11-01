package com.bakery.bakeryordersystem.controller;

import com.bakery.bakeryordersystem.model.BakeryProduct;
import com.bakery.bakeryordersystem.repository.ProductRepository;
import com.bakery.bakeryordersystem.service.ReviewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ShopController {

    private final ProductRepository productRepository;
    private final ReviewService reviews;

    public ShopController(ProductRepository productRepository, ReviewService reviews) {
        this.productRepository = productRepository;
        this.reviews = reviews;
    }

    @GetMapping("/shop")
    public String shop(@RequestParam(required = false) String category,
                       @RequestParam(required = false) String q,
                       Model model) {

        String cat   = category == null ? "" : category.trim().toUpperCase();
        String query = q == null ? "" : q.trim().toLowerCase();

        List<BakeryProduct> items = productRepository.findAll().stream()
                .filter(p -> cat.isEmpty()
                        || String.valueOf(p.getCategory()).equalsIgnoreCase(cat))
                .filter(p -> query.isEmpty()
                        || ((p.getName() != null ? p.getName() : "") + " " + (p.getDetail() != null ? p.getDetail() : "")).toLowerCase().contains(query))
                .collect(Collectors.toList());

        String title;
        switch (cat) {
            case "BREAD":  title = "Bread & Savouries";        break;
            case "CAKE":   title = "Cakes";                    break;
            case "PASTRY": title = "Pastries & Cake Pieces";   break;
            default:       title = query.isEmpty() ? "All Products" : "Search: " + (q != null ? q.trim() : "");
        }

        UriComponentsBuilder back = UriComponentsBuilder.fromPath("/shop");   // where "Add +" returns to
        if (!cat.isEmpty()) back.queryParam("category", cat);
        if (!query.isEmpty()) back.queryParam("q", q.trim());
        model.addAttribute("backUrl", back.build().encode().toUriString());
        model.addAttribute("items", items);
        model.addAttribute("category", cat);
        model.addAttribute("title", title);
        model.addAttribute("ratings", reviews.summaries());   // productId -> average rating + count

        return "shop";
    }
}