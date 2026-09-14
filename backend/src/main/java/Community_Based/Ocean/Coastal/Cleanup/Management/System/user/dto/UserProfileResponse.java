package Community_Based.Ocean.Coastal.Cleanup.Management.System.user.dto;

import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.enums.UserRole;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.enums.UserStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Fields common to every role. Role-specific extras (diver equipment, org registration details,
 * etc.) come back in a separate role-specific profile DTO alongside this one, not embedded here.
 */
public record UserProfileResponse(
        Integer userId,
        String firstName,
        String lastName,
        String email,
        String phone,
        UserRole role,
        UserStatus status,
        LocalDate dateOfBirth,
        String addressLine,
        BigDecimal latitude,
        BigDecimal longitude,
        LocalDateTime createdDate
) {
}
