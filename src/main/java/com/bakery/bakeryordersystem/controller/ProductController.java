package com.bakery.bakeryordersystem.controller;
import com.bakery.bakeryordersystem.service.ProductImages;
import com.bakery.bakeryordersystem.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
@Controller @RequestMapping("/admin/products")
public class ProductController {
    private final ProductService service; private final ProductImages images;
    public ProductController(ProductService service, ProductImages images) { this.service = service; this.images = images; }
    @GetMapping public String list(@RequestParam(required = false) String q, @RequestParam(required = false) String edit, Model m) {
        m.addAttribute("items", service.search(q)); m.addAttribute("q", q);
        m.addAttribute("item", edit == null ? null : service.find(edit));
        m.addAttribute("images", images);          // lets the page show each product's photo
        return "products";
    }
    @PostMapping("/save") public String save(@RequestParam(required = false) String id, @RequestParam String category, @RequestParam String name, @RequestParam double price, @RequestParam int stock, @RequestParam String detail,
                                             @RequestParam(required = false) MultipartFile photo, RedirectAttributes ra) {
        try { service.save(id, category, name, price, stock, detail, photo); }
        catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return id == null || id.isBlank() ? "redirect:/admin/products" : "redirect:/admin/products?edit=" + id;
        }
        ra.addFlashAttribute("saved", "Product saved.");
        return "redirect:/admin/products";
    }
    @PostMapping("/delete/{id}") public String delete(@PathVariable String id) { service.delete(id); return "redirect:/admin/products"; }
}
