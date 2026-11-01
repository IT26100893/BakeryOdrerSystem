package com.bakery.bakeryordersystem.controller;
import com.bakery.bakeryordersystem.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

/** Public home page: shows up to 5 in-stock products that have a photo. */
@Controller
public class HomeController {
    private final ProductService products; private final ReviewService reviews; private final ProductImages images;
    public HomeController(ProductService products, ReviewService reviews, ProductImages images) { this.products = products; this.reviews = reviews; this.images = images; }

    public static class Pick {
        private final String id, name, image; private final double price;
        public Pick(String id, String name, double price, String image) { this.id = id; this.name = name; this.price = price; this.image = image; }
        public String getId() { return id; }
        public String getName() { return name; }
        public double getPrice() { return price; }
        public String getImage() { return image; }
    }

    @GetMapping("/") public String home(Model m) {
        List<Pick> picks = products.search(null).stream().filter(p -> p.getStock() > 0)
                .map(p -> new Pick(p.getId(), p.getName(), p.getPrice(), images.forProduct(p)))
                .filter(p -> p.getImage() != null).limit(5).toList();
        m.addAttribute("picks", picks);
        m.addAttribute("highlights", reviews.highlights(3));      // newest 4-5 star reviews
        m.addAttribute("overall", reviews.overall());
        return "index";
    }
}
