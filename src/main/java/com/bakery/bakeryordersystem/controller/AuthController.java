package com.bakery.bakeryordersystem.controller;
import com.bakery.bakeryordersystem.model.User;
import com.bakery.bakeryordersystem.service.LoginVerificationService;
import com.bakery.bakeryordersystem.service.LoginVerificationService.Pending;
import com.bakery.bakeryordersystem.service.UserService;
import jakarta.servlet.http.*;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Optional;

/** Public pages: login (password + e-mailed code), register, logout. */
@Controller
public class AuthController {
    private final UserService users;
    private final LoginVerificationService verification;
    public AuthController(UserService users, LoginVerificationService verification) { this.users = users; this.verification = verification; }

    @GetMapping("/login") public String loginPage(HttpSession s) {
        Object role = s.getAttribute("role");
        return role == null ? "login" : "redirect:" + ("ADMIN".equals(role) ? "/admin" : "/customer");
    }

    /** Step 1: e-mail + password. If correct, a 6-digit code is e-mailed and the user is sent to the code page (NOT logged in yet). */
    @PostMapping("/login") public String login(@RequestParam String email, @RequestParam String password, HttpServletRequest req, Model m) {
        Optional<User> u = users.authenticate(email, password);
        if (u.isEmpty()) { m.addAttribute("error", "Wrong email or password"); m.addAttribute("email", email); return "login"; }
        try { verification.start(req.getSession(), u.get()); }
        catch (MailException e) {
            m.addAttribute("error", "We could not send the verification e-mail. Please try again in a moment.");
            m.addAttribute("email", email); return "login";
        }
        return "redirect:/login/verify";
    }

    @GetMapping("/login/verify") public String verifyPage(HttpSession s, Model m) {
        if (s.getAttribute("role") != null) return "redirect:/";
        Optional<Pending> p = verification.pending(s);
        if (p.isEmpty()) return "redirect:/login";
        m.addAttribute("maskedEmail", LoginVerificationService.mask(p.get().getEmail()));
        return "login-verify";
    }

    /** Step 2: the typed code. Only now is the user really logged in. */
    @PostMapping("/login/verify") public String verify(@RequestParam String code, HttpServletRequest req, Model m, RedirectAttributes ra) {
        HttpSession old = req.getSession();
        Pending p;
        try { p = verification.verify(old, code); }
        catch (IllegalArgumentException e) {            // wrong code - stay on the page
            m.addAttribute("error", e.getMessage());
            verification.pending(old).ifPresent(x -> m.addAttribute("maskedEmail", LoginVerificationService.mask(x.getEmail())));
            return "login-verify";
        } catch (IllegalStateException e) {             // expired / too many tries / nothing pending - start again
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/login";
        }
        String next = (String) old.getAttribute("next");
        req.changeSessionId();                           // new session id after login (prevents session fixation)
        HttpSession s = req.getSession();
        s.setAttribute("uid", p.getUid()); s.setAttribute("role", p.getRole()); s.setAttribute("uname", p.getName());
        s.removeAttribute("next");
        if ("ADMIN".equals(p.getRole())) return "redirect:/admin";
        return "redirect:" + (next != null && next.startsWith("/customer") ? next : "/customer");
    }

    @PostMapping("/login/resend") public String resend(HttpSession s, RedirectAttributes ra) {
        try { verification.resend(s); ra.addFlashAttribute("msg", "A new code has been sent."); }
        catch (IllegalArgumentException e) { ra.addFlashAttribute("error", e.getMessage()); }
        catch (IllegalStateException e) { ra.addFlashAttribute("error", e.getMessage()); return "redirect:/login"; }
        catch (MailException e) { ra.addFlashAttribute("error", "We could not send the e-mail. Please try again in a moment."); }
        return "redirect:/login/verify";
    }

    @GetMapping("/register") public String registerPage(HttpSession s) {
        return s.getAttribute("role") == null ? "register" : "redirect:/";
    }
    @PostMapping("/register") public String register(@RequestParam String name, @RequestParam String email, @RequestParam String phone,
                                                     @RequestParam String password, @RequestParam String confirm, Model m, RedirectAttributes ra) {
        try {
            if (!password.equals(confirm)) throw new IllegalArgumentException("The two passwords do not match");
            users.register(name, email, phone, password);
        } catch (IllegalArgumentException e) {
            m.addAttribute("error", e.getMessage()); m.addAttribute("name", name); m.addAttribute("email", email); m.addAttribute("phone", phone);
            return "register";
        }
        ra.addFlashAttribute("msg", "Account created. Please log in.");
        return "redirect:/login";
    }
    @PostMapping("/logout") public String logout(HttpSession s) { s.invalidate(); return "redirect:/"; }
}
