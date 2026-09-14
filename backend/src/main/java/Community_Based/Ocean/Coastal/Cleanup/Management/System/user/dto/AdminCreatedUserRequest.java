package Community_Based.Ocean.Coastal.Cleanup.Management.System.user.dto;

import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.enums.UserRole;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Same shape as RegisterRequest, for the admin-only "create an ADMIN/GOVERNMENT_OFFICER account"
 * endpoint (see CLAUDE.md's Auth mechanism notes — the bootstrap admin from env vars is separate,
 * this DTO is for admin-created accounts after that). Only ADMIN and GOVERNMENT_OFFICER are legal
 * values for {@code role} here — enforced at the service layer, same reasoning as RegisterRequest.
 */
public record AdminCreatedUserRequest(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 100) String password,
        @Size(max = 30) String phone,
        @NotNull UserRole role,
        @Valid RoleDetails roleDetails
) {

    /** Only meaningful when role=GOVERNMENT_OFFICER — ADMIN has no extra profile columns. */
    public record RoleDetails(
            @Size(max = 255) String department,
            @Size(max = 255) String designation
    ) {
    }
}
