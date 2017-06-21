package com.amazonaws.lambda.userfilterlambda;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Description.
 * @author Your Name
 */
public class PushTokenTest {
    private final String message = "test";
    private final String tenantKey = "dev";
    private final String topic = "cosign";
    private final long userId = 1234L;

    @Test
    public void testGetterAndSetter() {
        final PushToken token = new PushToken();
        token.setMessage(message);
        token.setTenantKey(tenantKey);
        token.setTopic(topic);
        token.setUserId(userId);
        assertEquals(token.getMessage(), message);
        assertEquals(token.getTenantKey(), tenantKey);
        assertEquals(token.getTopic(), topic);
        assertEquals(token.getUserId(), userId);
    }

    @Test
    public void testToString() {
        final PushToken token = new PushToken();
        token.setMessage(message);
        token.setTenantKey(tenantKey);
        token.setTopic(topic);
        token.setUserId(userId);
        assertEquals(token.toString(),
                "tenantKey:" + tenantKey + ", userId:" + userId + ", topic:" + topic + ", messaage:" + message);
    }
}
