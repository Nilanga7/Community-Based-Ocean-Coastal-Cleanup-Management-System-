package lk.tideline.cleanup.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lk.tideline.cleanup.model.CertificationLevel;
import lk.tideline.cleanup.model.OrganizationType;
import lk.tideline.cleanup.model.Role;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank String fullName,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, message = "must be at least 8 characters") String password,
            String phone,
            Role role,
            String province,
            String city,
            Double latitude,
            Double longitude,
            String organizationName,
            OrganizationType organizationType,
            CertificationLevel certificationLevel,
            @Size(max = 300) String websiteUrl
    ) {
    }

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password,
            /** Keeps the session for 30 days instead of 12 hours. */
            Boolean rememberMe
    ) {
        public boolean remember() {
            return Boolean.TRUE.equals(rememberMe);
        }
    }

    public record ForgotPasswordRequest(@NotBlank @Email String email) {
    }

    public record ResetPasswordRequest(
            @NotBlank String token,
            @NotBlank @Size(min = 8, message = "must be at least 8 characters") String password
    ) {
    }

    /** The token is absent for divers and organisations, who wait for administrator verification. */
    public record AuthResponse(
            String token,
            long expiresInSeconds,
            UserDtos.UserResponse user
    ) {
    }
}
