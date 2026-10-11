package lk.tideline.cleanup.service;

import lk.tideline.cleanup.config.TidelineProperties;
import lk.tideline.cleanup.model.PasswordResetToken;
import lk.tideline.cleanup.model.User;
import lk.tideline.cleanup.repository.PasswordResetTokenRepository;
import lk.tideline.cleanup.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;

/** Forgot password: a one-time link, valid for a short time, that sets a new password. */
@Service
public class PasswordResetService {

    static final long VALID_MINUTES = 30;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository users;
    private final PasswordResetTokenRepository tokens;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetMailer mailer;
    private final TidelineProperties properties;

    public PasswordResetService(UserRepository users, PasswordResetTokenRepository tokens, PasswordEncoder passwordEncoder,
                                PasswordResetMailer mailer, TidelineProperties properties) {
        this.users = users;
        this.tokens = tokens;
        this.passwordEncoder = passwordEncoder;
        this.mailer = mailer;
        this.properties = properties;
    }

    /**
     * Sends a reset link if the email belongs to an account. The caller always gets the same answer,
     * so the form can't be used to find out who has an account.
     */
    @Transactional
    public void requestReset(String email) {
        User user = users.findByEmailIgnoreCase(email.trim()).orElse(null);
        if (user == null || user.isSuspended()) {
            return;
        }
        Instant now = Instant.now();
        // Only the newest link works.
        tokens.findByUserAndUsedAtIsNull(user).forEach(old -> old.setUsedAt(now));

        byte[] raw = new byte[32];
        RANDOM.nextBytes(raw);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);

        PasswordResetToken reset = new PasswordResetToken();
        reset.setUser(user);
        reset.setTokenHash(hash(token));
        reset.setExpiresAt(now.plus(VALID_MINUTES, ChronoUnit.MINUTES));
        tokens.save(reset);

        String base = properties.getFrontendUrl().replaceAll("/+$", "");
        mailer.send(user, base + "/reset-password?token=" + token, VALID_MINUTES);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken reset = tokens.findByTokenHash(hash(token))
                .filter(found -> found.getUsedAt() == null && found.getExpiresAt().isAfter(Instant.now()))
                .orElseThrow(() -> new IllegalArgumentException(
                        "This reset link is invalid or has expired. Ask for a new one."));
        User user = reset.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        reset.setUsedAt(Instant.now());
        users.save(user);
    }

    static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
