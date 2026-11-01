package com.bakery.bakeryordersystem.controller;
import com.bakery.bakeryordersystem.service.CustomCakeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
@Controller @RequestMapping("/admin/bookings")
public class CustomCakeController {
    private final CustomCakeService service;
    public CustomCakeController(CustomCakeService service) { this.service = service; }
    @GetMapping public String list(@RequestParam(required = false) String q, @RequestParam(required = false) String edit, Model m) {
        m.addAttribute("items", service.search(q)); m.addAttribute("q", q);
        m.addAttribute("item", edit == null ? null : service.find(edit).orElse(null));
        return "bookings";
    }
    @PostMapping("/save") public String save(@RequestParam(required = false) String id, @RequestParam String customerName, @RequestParam String phone, @RequestParam String flavour,
                                             @RequestParam double sizeKg, @RequestParam(defaultValue = "") String message, @RequestParam String pickupDate, @RequestParam String status, RedirectAttributes ra) {
        try { service.save(id, customerName, phone, flavour, sizeKg, message, pickupDate, status); ra.addFlashAttribute("msg", "Booking saved."); }
        catch (IllegalArgumentException e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/admin/bookings";
    }
    /** Printable summary of one booking. */
    @GetMapping("/detail/{id}") public String detail(@PathVariable String id, Model m) {
        var b = service.find(id);
        if (b.isEmpty()) return "redirect:/admin/bookings";
        m.addAttribute("b", b.get()); m.addAttribute("backUrl", "/admin/bookings");
        return "booking-detail";
    }
    @PostMapping("/delete/{id}") public String delete(@PathVariable String id, RedirectAttributes ra) { service.delete(id); ra.addFlashAttribute("msg", "Booking deleted."); return "redirect:/admin/bookings"; }
}
