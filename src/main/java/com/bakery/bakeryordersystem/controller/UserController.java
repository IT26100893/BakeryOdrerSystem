package com.bakery.bakeryordersystem.controller;
import com.bakery.bakeryordersystem.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** ADMIN area: manage customer and admin accounts. */
@Controller @RequestMapping("/admin/users")
public class UserController {
    private final UserService service;
    public UserController(UserService service) { this.service = service; }

    @GetMapping public String list(@RequestParam(required = false) String q, @RequestParam(required = false) String edit, Model m) {
        m.addAttribute("items", service.search(q)); m.addAttribute("q", q);
        m.addAttribute("item", edit == null ? null : service.find(edit));
        return "users";
    }
    @PostMapping("/save") public String save(@RequestParam(required = false) String id, @RequestParam String role, @RequestParam String name,
                                             @RequestParam String email, @RequestParam String phone,
                                             @RequestParam(required = false) String password, RedirectAttributes ra) {
        try { service.save(id, role, name, email, phone, password); }
        catch (IllegalArgumentException e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/admin/users";
    }
    @PostMapping("/delete/{id}") public String delete(@PathVariable String id, HttpSession session, RedirectAttributes ra) {
        if (id.equals(session.getAttribute("uid"))) ra.addFlashAttribute("error", "You cannot delete the account you are logged in with");
        else service.delete(id);
        return "redirect:/admin/users";
    }
}
