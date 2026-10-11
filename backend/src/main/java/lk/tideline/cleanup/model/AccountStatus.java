package lk.tideline.cleanup.model;

/**
 * Volunteer divers and organisations start as PENDING_REVIEW until an administrator checks their
 * certificates or website. Every other account is APPROVED on creation.
 */
public enum AccountStatus {
    PENDING_REVIEW,
    APPROVED,
    REJECTED
}
