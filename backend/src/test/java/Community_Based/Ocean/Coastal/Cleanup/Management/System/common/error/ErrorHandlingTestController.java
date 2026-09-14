package Community_Based.Ocean.Coastal.Cleanup.Management.System.common.error;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.NO_CONTENT;

/**
 * Test-only — never shipped in the main jar (compiled under src/test/java). Exists solely to give
 * GlobalExceptionHandlerTest and SecurityConfigTest real endpoints to hit, since no production
 * controllers exist yet at this step. Requires auth like any other endpoint (nothing here is
 * permitAll), so tests must send a valid Bearer token.
 */
@RestController
@RequestMapping("/test-only")
public class ErrorHandlingTestController {

    @PostMapping("/validate")
    @ResponseStatus(NO_CONTENT)
    public void validate(@Valid @RequestBody ValidatedRequest request) {
        // Reaching here means validation passed — nothing else to do for this test endpoint.
    }

    @GetMapping("/not-found")
    public void notFound() {
        throw new EntityNotFoundException("test-only: no such resource");
    }

    @GetMapping("/boom")
    public void boom() {
        throw new RuntimeException("test-only: forced unexpected exception");
    }

    @GetMapping("/admin-only")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(NO_CONTENT)
    public void adminOnly() {
        // Reaching here means the caller held ROLE_ADMIN.
    }

    public record ValidatedRequest(@NotBlank String requiredField) {
    }
}
