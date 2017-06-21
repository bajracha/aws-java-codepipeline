package com.amazonaws.lambda.userfilterlambda;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

/**
 * Description.
 * @author Your Name
 */
@SuppressWarnings("nls")
public class UserFilterLambda implements RequestHandler<PushToken, String> {
    private final String TABLE_NAME = "PNC_SUBSCRIBER";
    private final String COL_PARTITION_KEY = "TENANTID_PERSONID";// "TENANT_PERSONID_BYTE";
    private final String COL_SORT_KEY = "TOPIC";
    private final Regions REGION = Regions.US_WEST_2;

    /**
     * {@inheritDoc}
     */
    @Override
    public String handleRequest(final PushToken token, final Context context) {
        context.getLogger().log("Input: " + token);

        final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion(REGION).build();
        final DynamoDB dynamoDB = new DynamoDB(client);
        final Table table = dynamoDB.getTable(TABLE_NAME);

        try {
            // TODO: getKey method will be replaced by a new service or lambda function
            final String partitionKey = getKey(token);
            // final byte[] partitionKey = "L3Byb2QvMTIzNA==".getBytes("UTF-8");

            // final byte[] binaryKey = partitionKey.getBytes("UTF-8");
            // final ByteBuffer buffer = ByteBuffer.allocate(binaryKey.length);
            // buffer.put(binaryKey, 0, binaryKey.length);
            // buffer.position(0);

            final HashMap<String, String> nameMap = new HashMap<String, String>();
            nameMap.put("#partitionKey", COL_PARTITION_KEY);
            nameMap.put("#sortKey", COL_SORT_KEY);

            // final HashMap<String, Object> valueMap = new HashMap<String, Object>();
            // valueMap.put(":keyId", partitionKey.getBytes("UTF-8"));
            // context.getLogger().log("partitionKey converts to string: " + partitionKey.toString());
            // valueMap.put(":topic", token.getTopic());
            // context.getLogger().log("topic: " + token.getTopic());

            final ValueMap valueMap = new ValueMap();
            valueMap.withBinary(":keyId", partitionKey.getBytes("UTF-8"));
            valueMap.with(":topic", token.getTopic());
            context.getLogger().log("partitionKey converts to string: " + partitionKey.toString());
            context.getLogger().log("topic: " + token.getTopic());

            final QuerySpec querySpec = new QuerySpec()
                    .withKeyConditionExpression("#partitionKey = :keyId and #sortKey = :topic").withNameMap(nameMap)
                    .withValueMap(valueMap);

            ItemCollection<QueryOutcome> items = null;
            Iterator<Item> iterator = null;
            Item item = null;
            final StringBuilder result = new StringBuilder();

            context.getLogger().log("Searching subscriptions for " + partitionKey);
            items = table.query(querySpec);

            iterator = items.iterator();
            context.getLogger().log("has next: " + iterator.hasNext());
            while (iterator.hasNext()) {
                item = iterator.next();
                result.append("return item: " + new String(item.getBinary(COL_PARTITION_KEY)) + " : "
                        + item.getString(COL_SORT_KEY) + " : " + item.getString("INFO"));
                context.getLogger().log(result.toString());
                // TODO: get topic arn and send a message with token info
            }
            return result.toString();

        } catch (final Exception e) {
            context.getLogger().log("Unable to query");
            context.getLogger().log(e.getMessage());
        }

        return "Error";
    }

    /*
     * TODO: This private method will be replaced by a new service or lambda function to generate key for query
     */
    // private byte[] getKey(final PushToken token) throws UnsupportedEncodingException {
    // // individual account
    // // return new StringBuilder().append("prod/").append(token.getTenantKey()).append("/").append(token.getUserId())
    // // .toString();
    //
    // // cerner cloud
    // final String sKey = new StringBuilder().append("/prod/").append(token.getUserId()).toString();
    // final byte[] encodedKey = Base64.encode(sKey.getBytes("UTF-8"));
    // return encodedKey;
    // }

    private String getKey(final PushToken token) throws UnsupportedEncodingException {
        // individual account
        // return new StringBuilder().append("prod/").append(token.getTenantKey()).append("/").append(token.getUserId())
        // .toString();

        // cerner cloud
        final String sKey = new StringBuilder().append("/").append(token.getTenantKey()).append("/")
                .append(token.getUserId()).toString();
        // final String sKey = "/prod/1234";
        return sKey;
    }

}
