package Community_Based.Ocean.Coastal.Cleanup.Management.System.user.controller;

import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.config.RequestAuthorization;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.user.dto.DiverEquipmentRequest;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.user.dto.DiverEquipmentResponse;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.user.service.DiverEquipmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Adding equipment is self-only (you manage your own gear list); listing is self-or-elevated,
 * matching UserProfileController's viewing rule (an ADMIN/GOVERNMENT_OFFICER may need to see a
 * diver's equipment during review).
 */
@RestController
@RequestMapping("/users/{id}/diver-equipment")
public class DiverEquipmentController {

    private final DiverEquipmentService diverEquipmentService;

    public DiverEquipmentController(DiverEquipmentService diverEquipmentService) {
        this.diverEquipmentService = diverEquipmentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DiverEquipmentResponse addEquipment(
            @PathVariable("id") Integer userId,
            @Valid @RequestBody DiverEquipmentRequest request,
            Authentication authentication
    ) {
        RequestAuthorization.requireSelf(userId, authentication, "You can only manage your own equipment");
        return diverEquipmentService.addEquipment(userId, request);
    }

    @GetMapping
    public List<DiverEquipmentResponse> listEquipment(
            @PathVariable("id") Integer userId,
            Authentication authentication
    ) {
        RequestAuthorization.requireSelfOrElevated(
                userId, authentication, "You do not have permission to view this diver's equipment"
        );
        return diverEquipmentService.listEquipment(userId);
    }
}
