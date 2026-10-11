package lk.tideline.cleanup.model;

/** What an uploaded account document is, so a reviewer sees each kind in its own group. */
public enum DocumentKind {
    /** A diving qualification, such as a PADI card or logbook page. */
    CERTIFICATE,
    /** A licence a diver holds, such as a commercial diving or boat licence. */
    LICENCE,
    /** Proof that an administrator or government officer holds their post. */
    APPOINTMENT
}
