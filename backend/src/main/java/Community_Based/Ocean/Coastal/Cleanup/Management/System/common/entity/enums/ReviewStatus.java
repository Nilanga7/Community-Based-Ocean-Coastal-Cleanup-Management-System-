package Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.enums;

/**
 * Shared by registration_verification.status and cleanup_request_post.stage_status —
 * both tables use this identical value set for their approve/reject/clarify pipeline.
 */
public enum ReviewStatus {
    PENDING,
    NEEDS_CLARIFICATION,
    RESUBMITTED,
    APPROVED,
    REJECTED
}
