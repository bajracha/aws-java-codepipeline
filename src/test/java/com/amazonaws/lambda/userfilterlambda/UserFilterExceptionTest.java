package com.amazonaws.lambda.userfilterlambda;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Description.
 * @author Your Name
 */
public class UserFilterExceptionTest {
    @Test
    public void testConstructor_withMessage() {
        final String errorMessage = "This is the error message";
        final UserFilterException exception = new UserFilterException(errorMessage);
        assertTrue(exception instanceof RuntimeException);
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    public void testConstructor_withMessageAndException() {
        final String errorMessage = "This is the error message";
        final NullPointerException nullPointer = new NullPointerException();
        final UserFilterException exception = new UserFilterException(errorMessage, nullPointer);
        assertTrue(exception.getCause().equals(nullPointer));
        assertEquals(errorMessage, exception.getMessage());
    }
}
