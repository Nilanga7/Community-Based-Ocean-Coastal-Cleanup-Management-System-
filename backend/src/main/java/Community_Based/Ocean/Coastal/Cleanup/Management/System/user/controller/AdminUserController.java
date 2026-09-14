package Community_Based.Ocean.Coastal.Cleanup.Management.System.user.controller;

import Community_Based.Ocean.Coastal.Cleanup.Management.System.user.dto.AdminCreatedUserRequest;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.user.dto.UserProfileResponse;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.user.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin-only account provisioning for ADMIN/GOVERNMENT_OFFICER — these two roles are not
 * self-registerable (see RegisterRequest's Javadoc). Unauthenticated/wrong-role requests are
 * already denied 401/403 by SecurityConfig's default-deny + this method's own @PreAuthorize,
 * using the same JSON error shape as every other endpoint (see GlobalExceptionHandler).
 */
@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

    private final AuthService authService;

    public AdminUserController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public UserProfileResponse createAdminOrGovernmentOfficerAccount(@Valid @RequestBody AdminCreatedUserRequest request) {
        return authService.createAdminOrGovernmentOfficerAccount(request);
    }
}
