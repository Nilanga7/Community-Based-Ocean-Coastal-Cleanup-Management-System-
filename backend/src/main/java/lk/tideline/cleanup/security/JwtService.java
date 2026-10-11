package lk.tideline.cleanup.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lk.tideline.cleanup.config.TidelineProperties;
import lk.tideline.cleanup.model.User;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expiryMinutes;
    private final long rememberMeMinutes;

    public JwtService(TidelineProperties properties) {
        String secret = properties.getSecurity().getJwtSecret();
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("TIDELINE_JWT_SECRET must be set to at least 32 characters. "
                    + "Generate one with: openssl rand -base64 48");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiryMinutes = properties.getSecurity().getJwtExpiryMinutes();
        this.rememberMeMinutes = properties.getSecurity().getRememberMeDays() * 24 * 60;
    }

    public String issueToken(User user) {
        return issueToken(user, false);
    }

    /** "Remember me" gets a long-lived session; otherwise the usual working-day length. */
    public String issueToken(User user, boolean rememberMe) {
        long minutes = rememberMe ? rememberMeMinutes : expiryMinutes;
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("uid", user.getId())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(minutes, ChronoUnit.MINUTES)))
                .signWith(key)
                .compact();
    }

    public String extractEmail(String token) {
        return parse(token).getSubject();
    }

    public long expirySeconds() {
        return expiryMinutes * 60;
    }

    public long expirySeconds(boolean rememberMe) {
        return (rememberMe ? rememberMeMinutes : expiryMinutes) * 60;
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
