package Community_Based.Ocean.Coastal.Cleanup.Management.System.common.error;

/**
 * Throw for a business-rule validation failure that Bean Validation on the DTO can't express
 * (e.g. a role-conditional required field, like organizationName being required only when
 * role=ORGANIZATION) — GlobalExceptionHandler maps this to 400, the same ErrorCode.VALIDATION_ERROR
 * shape as an ordinary Bean Validation failure, using the exception's own message.
 */
public class BusinessValidationException extends RuntimeException {

    public BusinessValidationException(String message) {
        super(message);
    }
}
