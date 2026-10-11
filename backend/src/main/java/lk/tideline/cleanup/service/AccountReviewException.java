package lk.tideline.cleanup.service;

/** Sign-in refused because the account is waiting for, or failed, administrator verification. */
public class AccountReviewException extends RuntimeException {

    private final String code;

    public AccountReviewException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
