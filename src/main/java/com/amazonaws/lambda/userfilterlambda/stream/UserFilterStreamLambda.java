package com.amazonaws.lambda.userfilterlambda.stream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.amazonaws.lambda.userfilterlambda.PushToken;
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
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.google.gson.Gson;

public class UserFilterStreamLambda implements RequestStreamHandler {
    private final String TABLE_NAME = System.getenv("PNC_TABLE_NAME");
    private final String COL_PARTITION_KEY = "TENANTID_PERSONID";// "TENANT_PERSONID_BYTE";
    private final String COL_SORT_KEY = "TOPIC";
    private final Regions REGION = Regions.fromName(System.getenv("REGION"));

    JSONParser parser = new JSONParser();

    @Override
    public void handleRequest(final InputStream inputStream, final OutputStream outputStream, final Context context)
            throws IOException {

        final LambdaLogger logger = context.getLogger();
        logger.log("Loading Java Lambda handler of ProxyWithStream");

        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        final JSONObject responseJson = new JSONObject();
        final String name = "World";
        final String responseCode = "200";

        // init DynamoDB
        final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion(REGION).build();
        final DynamoDB dynamoDB = new DynamoDB(client);
        final Table table = dynamoDB.getTable(TABLE_NAME);

        try {
            // parse input
            final JSONObject event = (JSONObject) parser.parse(reader);
            final String requestBodyJson = event.get("body").toString().replaceAll("\\\"", "\"");
            // validate(requestBodyJson);
            final PushToken token = new Gson().fromJson(requestBodyJson, PushToken.class);

            final String result = executeFilter(token, context, table);

            final JSONObject responseBody = new JSONObject();
            responseBody.put("input", result.toString());
            responseBody.put("message", "Success!");

            final JSONObject headerJson = new JSONObject();
            headerJson.put("x-custom-response-header", "my custom response header value");

            responseJson.put("statusCode", responseCode);
            responseJson.put("headers", headerJson);
            responseJson.put("body", responseBody.toString());

        } /*
           * catch (final ParseException pex) { final JSONObject headerJson = new JSONObject();
           * headerJson.put("x-custom-response-header", "my custom response header value"); final JSONObject
           * responseBody = new JSONObject(); responseBody.put("error", pex.toString()); responseJson.put("statusCode",
           * "400"); responseJson.put("headers", headerJson); responseJson.put("body", responseBody.toString()); }
           */catch (final Exception ex) {
            final JSONObject headerJson = new JSONObject();
            headerJson.put("x-custom-response-header", "my custom response header value");

            final JSONObject responseBody = new JSONObject();
            responseBody.put("error", ex.getMessage());
            logger.log(ex.getStackTrace().toString());

            responseJson.put("statusCode", "500");
            responseJson.put("headers", headerJson);
            responseJson.put("body", responseBody.toString());
        }

        logger.log(responseJson.toJSONString());
        final OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        writer.write(responseJson.toJSONString());
        writer.close();
    }

    // private void validate(final JSONObject requestBodyJson) {
    // assert requestBodyJson != null : "The request body cannnot be null";
    // assert requestBodyJson.get("tenantKey") != null : "tenantKey cannot be null";
    // assert requestBodyJson.get("topic") != null : "topic cannot be null";
    // assert requestBodyJson.get("userId") != null : "userId cannot be null";
    // }

    private String executeFilter(final PushToken token, final Context context, final Table table)
            throws UnsupportedEncodingException {
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
                //sendToSNS(token, item);
            }

        }
        return result.toString();
    }

    private String getKey(final PushToken token) throws UnsupportedEncodingException {
        // format the key based on the pattern defined in dynamoDB
        // final String sKey = "/prod/1234";
        final String sKey = new StringBuilder().append("/").append(token.getTenantKey()).append("/")
                .append(token.getUserId()).toString();

        return sKey;
    }

    private void sendToSNS(final PushToken token, final Item item) {
        final AmazonSNS snsClient = AmazonSNSClientBuilder.standard().withRegion(REGION).build();

        final String topicArn = item.getString("TOPIC_ARN");

        final PublishRequest publishRequest = new PublishRequest(topicArn, new Gson().toJson(token));
        snsClient.publish(publishRequest);
    }
}
