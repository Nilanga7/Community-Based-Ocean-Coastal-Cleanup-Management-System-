package Community_Based.Ocean.Coastal.Cleanup.Management.System.common.error;

/**
 * Throw when a request is well-formed and authenticated but the operation itself isn't allowed
 * (e.g. self-registering as ADMIN) — GlobalExceptionHandler maps this to 403 using the exception's
 * own message, unlike the generic AccessDeniedException handler which always uses
 * ErrorCode.FORBIDDEN's default message.
 */
public class ForbiddenOperationException extends RuntimeException {

    public ForbiddenOperationException(String message) {
        super(message);
    }
}
