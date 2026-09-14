package Community_Based.Ocean.Coastal.Cleanup.Management.System.user;

import Community_Based.Ocean.Coastal.Cleanup.Management.System.user.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.NO_CONTENT;

/**
 * Test-only — never shipped in the main jar (compiled under src/test/java). Exists solely to
 * exercise RegisterRequest's Bean Validation annotations through a real HTTP request (validation
 * failure -> GlobalExceptionHandler -> shared ErrorResponse shape), since the real
 * POST /auth/register controller doesn't exist yet.
 */
@RestController
@RequestMapping("/test-only")
public class RegisterRequestTestController {

    @PostMapping("/register")
    @ResponseStatus(NO_CONTENT)
    public void register(@Valid @RequestBody RegisterRequest request) {
        // Reaching here means validation passed — nothing else to do for this test endpoint.
    }
}
