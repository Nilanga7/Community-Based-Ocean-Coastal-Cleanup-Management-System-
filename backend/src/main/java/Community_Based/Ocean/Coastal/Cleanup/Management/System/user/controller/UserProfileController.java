package Community_Based.Ocean.Coastal.Cleanup.Management.System.user.controller;

import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.config.RequestAuthorization;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.user.dto.UpdateProfileRequest;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.user.dto.UserProfileResponse;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.user.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Viewing a profile is allowed for the owning user or an ADMIN/GOVERNMENT_OFFICER (e.g. during
 * review); editing is self-only — nobody else updates your profile on your behalf here.
 */
@RestController
@RequestMapping("/users/{id}/profile")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping
    public UserProfileResponse getProfile(@PathVariable("id") Integer userId, Authentication authentication) {
        RequestAuthorization.requireSelfOrElevated(
                userId, authentication, "You do not have permission to view this profile"
        );
        return userProfileService.getProfile(userId);
    }

    @PutMapping
    public UserProfileResponse updateProfile(
            @PathVariable("id") Integer userId,
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication
    ) {
        RequestAuthorization.requireSelf(userId, authentication, "You can only update your own profile");
        return userProfileService.updateProfile(userId, request);
    }
}
