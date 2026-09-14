package Community_Based.Ocean.Coastal.Cleanup.Management.System.common.error;

/**
 * The single source of truth for error {@code code}/default-{@code message} pairs used across
 * GlobalExceptionHandler and SecurityConfig's 401/403 writers — see ErrorResponse.
 */
public enum ErrorCode {

    VALIDATION_ERROR("Request validation failed"),
    NOT_FOUND("The requested resource was not found"),
    UNAUTHORIZED("Authentication required"),
    FORBIDDEN("You do not have permission to access this resource"),
    INTERNAL_ERROR("An unexpected error occurred");

    private final String defaultMessage;

    ErrorCode(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}
