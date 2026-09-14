package Community_Based.Ocean.Coastal.Cleanup.Management.System.common.error;

/**
 * The one error body shape for the whole API (see CLAUDE.md "API conventions"):
 * { "error": { "code": "...", "message": "..." } }. Every error response — whether it comes from
 * GlobalExceptionHandler (post-DispatcherServlet) or SecurityConfig's hand-written 401/403
 * writers (pre-DispatcherServlet) — must be built from this type, never a second ad-hoc shape.
 */
public record ErrorResponse(ErrorDetail error) {

    public record ErrorDetail(String code, String message) {
    }

    public static ErrorResponse of(ErrorCode code, String message) {
        return new ErrorResponse(new ErrorDetail(code.name(), message));
    }

    public static ErrorResponse of(ErrorCode code) {
        return of(code, code.defaultMessage());
    }
}
