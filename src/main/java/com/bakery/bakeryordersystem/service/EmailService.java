package com.bakery.bakeryordersystem.service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Sends the login verification e-mail.
 * If no SMTP server is configured (spring.mail.host is not set) the app runs in DEV mode:
 * nothing is e-mailed and the code is printed in the server console instead, so the project still runs on a laptop.
 * Any mail error is thrown to the caller (MailException), so the login is never completed without a delivered code.
 */
@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final ObjectProvider<JavaMailSender> mail;
    private final String from;

    public EmailService(ObjectProvider<JavaMailSender> mail,
                        @Value("${bakery.mail.from:}") String from,
                        @Value("${spring.mail.username:}") String username) {
        this.mail = mail;
        this.from = (from == null || from.isBlank()) ? username : from;
    }

    public boolean isConfigured() { return mail.getIfAvailable() != null; }

    public void sendLoginCode(String to, String name, String code, int minutesValid) {
        JavaMailSender sender = mail.getIfAvailable();
        if (sender == null) {
            log.warn("=== DEV MODE: e-mail is not configured. Login code for {} is {} (valid {} min) ===", to, code, minutesValid);
            return;
        }
        SimpleMailMessage m = new SimpleMailMessage();
        if (from != null && !from.isBlank()) m.setFrom(from);
        m.setTo(to);
        m.setSubject("Your Crumb & Co. login code");
        m.setText("Hi " + name + ",\n\n"
                + "Your verification code is: " + code + "\n\n"
                + "It is valid for " + minutesValid + " minutes. If you did not try to log in, please ignore this e-mail "
                + "and consider changing your password.\n\n"
                + "Crumb & Co. Bakery");
        sender.send(m);
    }
}
