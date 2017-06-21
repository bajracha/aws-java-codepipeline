package com.amazonaws.lambda.userfilterlambda;

/**
 * Description.
 * @author Your Name
 */
@SuppressWarnings("nls")
public class PushToken {
    String tenantKey;
    long userId;
    String topic;
    String message;

    /**
     * @return the tenantKey.
     */
    public String getTenantKey() {
        return tenantKey;
    }

    /**
     * @param tenantKey the tenantKey to set.
     */
    public void setTenantKey(final String tenantKey) {
        this.tenantKey = tenantKey;
    }

    /**
     * @return the userId.
     */
    public long getUserId() {
        return userId;
    }

    /**
     * @param userId the userId to set.
     */
    public void setUserId(final long userId) {
        this.userId = userId;
    }

    /**
     * @return the topic.
     */
    public String getTopic() {
        return topic;
    }

    /**
     * @param topic the topic to set.
     */
    public void setTopic(final String topic) {
        this.topic = topic;
    }

    /**
     * @return the message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set.
     */
    public void setMessage(final String message) {
        this.message = message;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "tenantKey:" + tenantKey + ", userId:" + userId + ", topic:" + topic + ", messaage:" + message;

    }

}
