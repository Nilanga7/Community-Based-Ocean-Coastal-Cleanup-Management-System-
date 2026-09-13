package Community_Based.Ocean.Coastal.Cleanup.Management.System.common.config;

import java.math.BigDecimal;

/**
 * Named constants for workflow rules confirmed in CLAUDE.md's "Working conventions" section —
 * never hardcode these values anywhere else.
 */
public final class BusinessConstants {

    private BusinessConstants() {
    }

    /** Community verification trust threshold: a post needs >= 75% true votes to be Community Verified. */
    public static final BigDecimal COMMUNITY_TRUST_THRESHOLD_PERCENTAGE = new BigDecimal("75.00");

    /** Community verification window: a post is forwarded to admin either way after 14 days. */
    public static final int COMMUNITY_VERIFICATION_WINDOW_DAYS = 14;

    /** Low-visibility public-prompt vote count: below 8 votes, surface the post to unregistered users too. */
    public static final int LOW_VISIBILITY_VOTE_THRESHOLD = 8;

    /** Initial alert radius, in kilometers, around a report's location. */
    public static final int INITIAL_ALERT_RADIUS_KM = 10;

    /** Radius escalation increment, in kilometers, applied each time the threshold isn't met. */
    public static final int ALERT_RADIUS_ESCALATION_INCREMENT_KM = 10;

    /** Escalation check interval, in days, between radius expansions. */
    public static final int ALERT_ESCALATION_CHECK_INTERVAL_DAYS = 14;
}
