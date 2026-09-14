package Community_Based.Ocean.Coastal.Cleanup.Management.System.common.config;

import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.error.ForbiddenOperationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * Shared "is this caller allowed to act on this userId" checks for controllers that expose
 * /users/{id}/... endpoints. JwtAuthenticationFilter sets the authenticated principal to the
 * token's userId directly (see that class) and grants a single ROLE_&lt;UserRole&gt; authority,
 * which is what these methods read.
 */
public final class RequestAuthorization {

    private RequestAuthorization() {
    }

    public static Integer principalUserId(Authentication authentication) {
        return (Integer) authentication.getPrincipal();
    }

    public static boolean hasElevatedRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ROLE_ADMIN") || authority.equals("ROLE_GOVERNMENT_OFFICER"));
    }

    public static void requireSelf(Integer pathUserId, Authentication authentication, String message) {
        if (!pathUserId.equals(principalUserId(authentication))) {
            throw new ForbiddenOperationException(message);
        }
    }

    public static void requireSelfOrElevated(Integer pathUserId, Authentication authentication, String message) {
        if (pathUserId.equals(principalUserId(authentication)) || hasElevatedRole(authentication)) {
            return;
        }
        throw new ForbiddenOperationException(message);
    }
}
