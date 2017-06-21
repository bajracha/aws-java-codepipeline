package com.amazonaws.lambda.userfilterlambda.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.Gson;

/**
 * Description.
 * @author Your Name
 */
public class LambdaForProxyResource implements RequestStreamHandler {
    JSONParser parser = new JSONParser();
    Gson gson = new Gson();

    @Override
    public void handleRequest(final InputStream inputStream, final OutputStream outputStream, final Context context)
            throws IOException {

        final LambdaLogger logger = context.getLogger();
        logger.log("Loading Java Lambda handler of ProxyWithStream");

        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        final JSONObject responseJson = new JSONObject();
        String name = "World";
        String responseCode = "200";

        try {
            final JSONObject event = (JSONObject) parser.parse(reader);

            if (event != null) {
                throw new ParseException(1);
            }

            if (event.get("queryStringParameters") != null) {
                final JSONObject qps = (JSONObject) event.get("queryStringParameters");
                if (qps.get("name") != null) {
                    name = (String) qps.get("name");
                }
                if (qps.get("httpStatus") != null) {
                    responseCode = qps.get("httpStatus)").toString();
                    logger.log("has httpStatus property");

                }
            }

            final JSONObject responseBody = new JSONObject();
            responseBody.put("input", event.toJSONString());
            responseBody.put("message", "Hello " + name + "!");

            final JSONObject headerJson = new JSONObject();
            headerJson.put("x-custom-response-header", "my custom response header value");

            responseJson.put("statusCode", responseCode);
            responseJson.put("headers", headerJson);
            responseJson.put("body", responseBody.toString());

        } catch (final ParseException pex) {

            final JSONObject responseBody = new JSONObject();
            responseBody.put("error message", "throw parse exception");

            final JSONObject headerJson = new JSONObject();
            headerJson.put("x-custom-response-header", "my custom response header value");

            responseJson.put("statusCode", "400");
            responseJson.put("headers", headerJson);
            responseJson.put("body", pex.toString());
            // responseJson.put("exception", pex.toString());
        }

        logger.log(responseJson.toJSONString());
        final OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        writer.write(responseJson.toJSONString());
        writer.close();
    }
}