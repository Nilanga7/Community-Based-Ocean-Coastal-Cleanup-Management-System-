package Community_Based.Ocean.Coastal.Cleanup.Management.System.user.service;

/**
 * Owned in spirit by the Admin Review module (RegistrationVerification/RegistrationReviewAction
 * are that module's tables per CLAUDE.md's Module scope table) — this interface is the one call
 * site the User Registration module needs (fired after a successful verification-document
 * upload), kept stable so the Admin Review module owner can extend the implementation (e.g.
 * notifying reviewers) without the upload flow needing to change.
 */
public interface RegistrationVerificationService {

    /**
     * Ensures a PENDING RegistrationVerification row exists for this user. Idempotent — a user
     * may upload multiple verification documents, but only one verification process per user.
     */
    void initiate(Integer userId);
}
