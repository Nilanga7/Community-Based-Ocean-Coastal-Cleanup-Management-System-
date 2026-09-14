package Community_Based.Ocean.Coastal.Cleanup.Management.System.user.dto;

import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Deliberately excludes firstName/lastName/email/password/role/status — those are either
 * identity-critical (need their own dedicated, more carefully-guarded flow) or not user-editable
 * at all. Every field here is optional; PUT only changes the fields actually supplied (null means
 * "leave as-is"), not "clear this field" — there's no way to null out addressLine, for example,
 * with this DTO shape.
 */
public record UpdateProfileRequest(
        @Size(max = 30) String phone,
        LocalDate dateOfBirth,
        @Size(max = 255) String addressLine,
        BigDecimal latitude,
        BigDecimal longitude
) {
}
