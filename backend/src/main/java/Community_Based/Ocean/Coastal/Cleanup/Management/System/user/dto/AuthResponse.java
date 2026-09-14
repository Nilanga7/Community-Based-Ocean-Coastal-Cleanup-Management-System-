package Community_Based.Ocean.Coastal.Cleanup.Management.System.user.dto;

import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.enums.UserRole;

/**
 * @param expiresIn access token lifetime in milliseconds (mirrors jwt.expiration-ms), not an
 *                   absolute timestamp
 */
public record AuthResponse(
        String token,
        long expiresIn,
        Integer userId,
        UserRole role
) {
}
