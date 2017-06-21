package com.amazonaws.lambda.userfilterlambda;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.amazonaws.services.lambda.runtime.Context;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
public class UserFilterLambdaTest {

    private static PushToken input;

    @BeforeClass
    public static void createInput() throws IOException {
        // TODO: set up your sample input object here.
        input = new PushToken();
        input.setTenantKey("client1");
        input.setUserId(1234);
        input.setMessage("test");
        input.setTopic("cosign");
    }

    private Context createContext() {
        final TestContext ctx = new TestContext();

        // TODO: customize your context here if needed.
        ctx.setFunctionName("Your Function Name");

        return ctx;
    }

    @Test
    public void testLambdaFunctionHandler() {
        final UserFilterLambda handler = new UserFilterLambda();
        final Context ctx = createContext();

        final String output = handler.handleRequest(input, ctx);

        // TODO: validate output here if needed.
        if (output != null) {
            System.out.println(output.toString());
        }
    }
}
