package lk.tideline.cleanup.service;

import lk.tideline.cleanup.config.TidelineProperties;
import lk.tideline.cleanup.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Sends the password reset link by email when a mail server is configured (spring.mail.host).
 * Without one — local development — the link is written to the server log instead.
 */
@Service
public class PasswordResetMailer {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetMailer.class);

    private final ObjectProvider<JavaMailSender> mailSender;
    private final TidelineProperties properties;

    public PasswordResetMailer(ObjectProvider<JavaMailSender> mailSender, TidelineProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    public void send(User user, String link, long validMinutes) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            log.info("No mail server configured. Password reset link for {}: {}", user.getEmail(), link);
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.getMail().getFrom());
        message.setTo(user.getEmail());
        message.setSubject("Reset your Tideline password");
        message.setText("Hi " + user.getFullName() + ",\n\n"
                + "Use this link to choose a new password. It works once and expires in " + validMinutes + " minutes.\n\n"
                + link + "\n\n"
                + "If you didn't ask to reset your password, you can ignore this email.");
        try {
            sender.send(message);
        } catch (MailException e) {
            // The response never says whether the email exists, so a delivery failure is only logged.
            log.warn("Could not send the password reset email to {}: {}", user.getEmail(), e.getMessage());
        }
    }
}
