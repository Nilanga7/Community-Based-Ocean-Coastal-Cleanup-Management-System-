package Community_Based.Ocean.Coastal.Cleanup.Management.System.common.error;

/**
 * Throw when a request conflicts with existing data (e.g. registering with an email already in
 * use) — GlobalExceptionHandler maps this to 409 with the exception's own message.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
