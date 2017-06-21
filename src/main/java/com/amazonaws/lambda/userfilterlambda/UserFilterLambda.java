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
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.google.gson.Gson;

/**
 * User Filter Lambda Function
 * @author Dongjian Cai (DC044031)
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

        final AmazonSNS snsClient = AmazonSNSClientBuilder.standard().withRegion(REGION).build();

        try {
            // TODO: getKey method will be replaced by a new lambda function
            final String partitionKey = getKey(token);

            // set up the query spec
            final HashMap<String, String> nameMap = new HashMap<String, String>();
            nameMap.put("#partitionKey", COL_PARTITION_KEY);
            nameMap.put("#sortKey", COL_SORT_KEY);

            final ValueMap valueMap = new ValueMap();
            valueMap.withBinary(":keyId", partitionKey.getBytes("UTF-8"));
            valueMap.with(":topic", token.getTopic());
            context.getLogger().log("INFO: partitionKey in string: " + partitionKey);
            context.getLogger().log("INFO: sortKey: " + token.getTopic());

            final QuerySpec querySpec = new QuerySpec()
                    .withKeyConditionExpression("#partitionKey = :keyId and #sortKey = :topic").withNameMap(nameMap)
                    .withValueMap(valueMap);

            ItemCollection<QueryOutcome> items = null;
            Iterator<Item> iterator = null;
            Item item = null;
            final StringBuilder result = new StringBuilder();

            // query dynamoDB
            items = table.query(querySpec);

            iterator = items.iterator();
            while (iterator.hasNext()) {
                item = iterator.next();

                if (item.getString("TOPIC_ARN") != null) {
                    result.append("tenantId_personId: " + new String(item.getBinary(COL_PARTITION_KEY)) + ", topic: "
                            + item.getString(COL_SORT_KEY) + ", message: " + item.getString("TOPIC_ARN"));
                    context.getLogger().log(result.toString());

                    // publish to topics with pushToken
                    sendToSNS(token, item, snsClient);
                }

            }
            return result.toString();

        } catch (final Exception e) {
            // final JSONObject responseJson = new JSONObject();
            // final JSONObject headerJson = new JSONObject();
            // headerJson.put("x-custom-response-header", "my custom response header value");
            //
            // final JSONObject responseBody = new JSONObject();
            // responseBody.put("error", e.toString());
            //
            // responseJson.put("statusCode", "400");
            // responseJson.put("headers", headerJson);
            // responseJson.put("body", responseBody.toString());
            // return responseJson.toString();

            context.getLogger().log("Errors during user filtering process");
            context.getLogger().log(e.getMessage());
            throw new UserFilterException("Errors during user filtering process", e);
        }
    }

    private String getKey(final PushToken token) throws UnsupportedEncodingException {
        // format the key based on the pattern defined in dynamoDB
        // final String sKey = "/prod/1234";
        final String sKey = new StringBuilder().append("/").append(token.getTenantKey()).append("/")
                .append(token.getUserId()).toString();

        return sKey;
    }

    private void sendToSNS(final PushToken token, final Item item, final AmazonSNS snsClient) {
        final String topicArn = item.getString("TOPIC_ARN");

        final PublishRequest publishRequest = new PublishRequest(topicArn, new Gson().toJson(token));
        snsClient.publish(publishRequest);
    }
}
