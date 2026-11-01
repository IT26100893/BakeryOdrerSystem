package com.bakery.bakeryordersystem.service;
import com.bakery.bakeryordersystem.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import java.io.Serializable;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Second login step: after the password is correct, a 6-digit code is e-mailed and must be typed in before the user is logged in.
 * The pending login lives only in the HTTP session (the code is stored as a salted hash, never in plain text).
 * Rules: code valid 10 minutes, 5 wrong tries, a new code can be requested every 30 seconds.
 */
@Service
public class LoginVerificationService {
    public static final int CODE_MINUTES = 10, MAX_ATTEMPTS = 5, RESEND_SECONDS = 30;
    private static final String KEY = "pendingLogin";
    private final SecureRandom rng = new SecureRandom();
    private final EmailService email;
    public LoginVerificationService(EmailService email) { this.email = email; }

    /** What we remember between "password OK" and "code OK". */
    public static final class Pending implements Serializable {
        private final String uid, role, name, email;
        private String codeHash; private Instant expires, lastSent; private int attempts;
        Pending(User u) { uid = u.getId(); role = u.getRole(); name = u.getName(); email = u.getEmail(); }
        public String getUid() { return uid; }
        public String getRole() { return role; }
        public String getName() { return name; }
        public String getEmail() { return email; }
    }

    public Optional<Pending> pending(HttpSession s) {
        Object o = s.getAttribute(KEY);
        return o instanceof Pending ? Optional.of((Pending) o) : Optional.empty();
    }

    /** Creates and e-mails a code. Throws a MailException if the e-mail cannot be sent (nothing is stored then). */
    public void start(HttpSession s, User u) {
        s.removeAttribute(KEY);
        Pending p = new Pending(u);
        issue(p);
        s.setAttribute(KEY, p);
    }

    /** Sends a fresh code (same pending login). */
    public void resend(HttpSession s) {
        Pending p = pending(s).orElseThrow(() -> new IllegalStateException("Your login session expired. Please log in again."));
        long wait = RESEND_SECONDS - Duration.between(p.lastSent, Instant.now()).getSeconds();
        if (wait > 0) throw new IllegalArgumentException("Please wait " + wait + " seconds before asking for a new code");
        issue(p);
    }

    /**
     * Checks the typed code. Returns the pending login on success (and forgets it).
     * IllegalArgumentException = wrong code, try again. IllegalStateException = expired / too many tries / nothing pending: start over.
     */
    public Pending verify(HttpSession s, String code) {
        Pending p = pending(s).orElseThrow(() -> new IllegalStateException("Your login session expired. Please log in again."));
        if (Instant.now().isAfter(p.expires)) { s.removeAttribute(KEY); throw new IllegalStateException("That code has expired. Please log in again."); }
        if (p.attempts >= MAX_ATTEMPTS) { s.removeAttribute(KEY); throw new IllegalStateException("Too many wrong codes. Please log in again."); }
        String typed = code == null ? "" : code.replaceAll("\\s", "");
        if (!typed.matches("\\d{6}") || !PasswordHasher.matches(typed, p.codeHash)) {
            p.attempts++;
            if (p.attempts >= MAX_ATTEMPTS) { s.removeAttribute(KEY); throw new IllegalStateException("Too many wrong codes. Please log in again."); }
            throw new IllegalArgumentException("Wrong code. " + (MAX_ATTEMPTS - p.attempts) + " tries left.");
        }
        s.removeAttribute(KEY);
        return p;
    }

    private void issue(Pending p) {
        String code = String.format("%06d", rng.nextInt(1_000_000));
        email.sendLoginCode(p.email, p.name, code, CODE_MINUTES);     // may throw: then the pending record is left unchanged
        p.codeHash = PasswordHasher.hash(code);
        p.expires = Instant.now().plus(Duration.ofMinutes(CODE_MINUTES));
        p.lastSent = Instant.now();
        p.attempts = 0;
    }

    /** nimal@mail.com -> n***@mail.com */
    public static String mask(String email) {
        int at = email == null ? -1 : email.indexOf('@');
        if (at < 1) return email == null ? "" : email;
        return email.charAt(0) + "***" + email.substring(at);
    }
}
