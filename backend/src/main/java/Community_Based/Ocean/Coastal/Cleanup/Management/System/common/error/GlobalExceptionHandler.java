package Community_Based.Ocean.Coastal.Cleanup.Management.System.common.error;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Every handler here returns ResponseEntity<ErrorResponse> directly and lets Spring's registered
 * message converter serialize it — do not reach for ObjectMapper/Jackson here (this project is on
 * Jackson 3, tools.jackson.*, only present via spring-boot-starter-jackson; returning the record
 * sidesteps that entirely). SecurityConfig's 401/403 writers can't go through this class (they run
 * before the DispatcherServlet) but build from the same ErrorResponse/ErrorCode types.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // @PreAuthorize (method security) throws this from inside the handler invocation itself —
    // i.e. after DispatcherServlet has already dispatched — so it never reaches SecurityConfig's
    // pre-DispatcherServlet AccessDeniedHandler; without this explicit handler the catch-all
    // Exception handler below would swallow it into a misleading 500 instead of a 403. URL-level
    // denials (authorizeHttpRequests) are still handled by SecurityConfig's AccessDeniedHandler,
    // since those are rejected before dispatch even happens; both paths produce the identical body.
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of(ErrorCode.FORBIDDEN));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        if (message.isBlank()) {
            message = ErrorCode.VALIDATION_ERROR.defaultMessage();
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(ErrorCode.VALIDATION_ERROR, message));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(ErrorCode.NOT_FOUND));
    }

    // Carries a specific, caller-facing message (e.g. "Admin and government officer accounts
    // cannot self-register") — unlike the generic AccessDeniedException handler above, which
    // always uses ErrorCode.FORBIDDEN's default message regardless of the exception's own text.
    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenOperation(ForbiddenOperationException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of(ErrorCode.FORBIDDEN, ex.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(ErrorCode.CONFLICT, ex.getMessage()));
    }

    // Same 400/VALIDATION_ERROR shape as handleValidation above, for business-rule checks that
    // aren't expressible as Bean Validation annotations on the DTO (e.g. a role-conditional
    // required field) — see AuthService.
    @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<ErrorResponse> handleBusinessValidation(BusinessValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(ErrorCode.VALIDATION_ERROR, ex.getMessage()));
    }

    // Thrown by AuthService.login() for both "no such email" and "wrong password" with the exact
    // same message — see AuthService, which deliberately doesn't distinguish the two so a client
    // can't use this endpoint to enumerate registered emails.
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of(ErrorCode.UNAUTHORIZED, ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(ErrorCode.INTERNAL_ERROR));
    }
}
