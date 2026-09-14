package Community_Based.Ocean.Coastal.Cleanup.Management.System.user.controller;

import Community_Based.Ocean.Coastal.Cleanup.Management.System.user.dto.AuthResponse;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.user.dto.LoginRequest;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.user.dto.RegisterRequest;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.user.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Both endpoints are permitAll in SecurityConfig — no Authorization header required or expected.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
