package com.amazonaws.lambda.userfilterlambda;

/**
 * Description.
 * @author Your Name
 */
public class UserFilterException extends RuntimeException {
    /**
     * The serialVersionUID.
     */
    private static final long serialVersionUID = -5332992353362355827L;

    /**
     * @param message message associated with the exception.
     * @param e exception.
     */
    public UserFilterException(final String message, final Throwable e) {
        super(message, e);
    }

    /**
     * @param message message associated with the exception.
     */
    public UserFilterException(final String message) {
        super(message);
    }
}
