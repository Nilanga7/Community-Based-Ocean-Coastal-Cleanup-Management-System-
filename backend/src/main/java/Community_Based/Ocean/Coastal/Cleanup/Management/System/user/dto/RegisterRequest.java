package Community_Based.Ocean.Coastal.Cleanup.Management.System.user.dto;

import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.enums.UserRole;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Public self-registration payload (POST /auth/register). Only VOLUNTEER_NON_DIVER,
 * VOLUNTEER_DIVER, and ORGANIZATION are legal values for {@code role} here — ADMIN and
 * GOVERNMENT_OFFICER accounts go through AdminCreatedUserRequest instead (see CLAUDE.md's
 * "Open technical decisions" / Auth mechanism notes). That restriction, and which
 * {@code roleDetails} fields are actually required for a given role (e.g. {@code organizationName}
 * when role=ORGANIZATION), are cross-field/business rules enforced at the service layer — Bean
 * Validation here only checks each field's own shape.
 */
public record RegisterRequest(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 100) String password,
        @Size(max = 30) String phone,
        @NotNull UserRole role,
        @Valid RoleDetails roleDetails
) {

    /**
     * One bag of optional per-role registration fields; only those matching {@code role} above
     * are meaningful for a given request — e.g. a VOLUNTEER_DIVER only populates
     * experience/certification/preferredRegion, an ORGANIZATION only populates
     * organizationName/organizationType/businessRegNum.
     */
    public record RoleDetails(
            @Size(max = 255) String availability,
            @Size(max = 255) String specialization,
            @Size(max = 255) String experience,
            @Size(max = 255) String certification,
            @Size(max = 255) String preferredRegion,
            @Size(max = 255) String organizationName,
            @Size(max = 100) String organizationType,
            @Size(max = 100) String businessRegNum
    ) {
    }
}
