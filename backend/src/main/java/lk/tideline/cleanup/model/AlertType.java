package lk.tideline.cleanup.model;

public enum AlertType {
    NEW_REPORT_NEARBY,
    REPORT_VERIFIED,
    ALERT_ESCALATED,
    PROJECT_PLANNED,
    PROJECT_UPDATE,
    AUTHORITY_DECISION,
    OPPORTUNITY,
    /** The decision on the recipient's own registration. */
    ACCOUNT_REVIEW,
    /** Someone else's registration waiting for an administrator to approve it. */
    ACCOUNT_APPLICATION,
    COMMENT_REPLY,
    INFO_REQUESTED,
    INFO_RESPONSE,
    RESOURCES_NEEDED,
    RESOURCES_ASSIGNED
}
