package lk.tideline.cleanup.controller;

import jakarta.validation.Valid;
import lk.tideline.cleanup.dto.AuthDtos.AuthResponse;
import lk.tideline.cleanup.dto.AuthDtos.ForgotPasswordRequest;
import lk.tideline.cleanup.dto.AuthDtos.ResetPasswordRequest;
import lk.tideline.cleanup.service.PasswordResetService;
import lk.tideline.cleanup.dto.AuthDtos.LoginRequest;
import lk.tideline.cleanup.dto.AuthDtos.RegisterRequest;
import lk.tideline.cleanup.dto.UserDtos.UserResponse;
import lk.tideline.cleanup.service.AuthService;
import lk.tideline.cleanup.service.CurrentUserService;
import lk.tideline.cleanup.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final CurrentUserService currentUser;
    private final PasswordResetService passwordReset;

    public AuthController(AuthService authService, UserService userService, CurrentUserService currentUser,
                          PasswordResetService passwordReset) {
        this.authService = authService;
        this.userService = userService;
        this.currentUser = currentUser;
        this.passwordReset = passwordReset;
    }

    /** Always the same answer, whether or not the email has an account. */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordReset.requestReset(request.email());
        return ResponseEntity.accepted().body(Map.of("message",
                "If an account uses that email, we've sent a link to reset the password."));
    }

    @PostMapping("/reset-password")
    public Map<String, String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordReset.resetPassword(request.token(), request.password());
        return Map.of("message", "Your password has been changed. Sign in with the new password.");
    }

    /** Multipart so applicants can attach files: a JSON "data" part plus "certificates" and "licences" files. */
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AuthResponse> register(@Valid @RequestPart("data") RegisterRequest request,
                                                 @RequestPart(value = "certificates", required = false) List<MultipartFile> certificates,
                                                 @RequestPart(value = "licences", required = false) List<MultipartFile> licences) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request, certificates, licences));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserResponse me() {
        return userService.view(currentUser.require().getId());
    }
}
