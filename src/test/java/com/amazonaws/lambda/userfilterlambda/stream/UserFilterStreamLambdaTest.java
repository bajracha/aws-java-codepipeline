// package com.amazonaws.lambda.userfilterlambda.stream;
//
// import java.io.ByteArrayInputStream;
// import java.io.ByteArrayOutputStream;
// import java.io.IOException;
// import java.io.InputStream;
// import java.io.OutputStream;
//
// import org.junit.Test;
//
// import com.amazonaws.lambda.userfilterlambda.TestContext;
//
/// **
// * A simple test harness for locally invoking your Lambda function handler.
// */
// public class UserFilterStreamLambdaTest {
//
// private static final String SAMPLE_INPUT_STRING = "{\"foo\":\"bar\"}";
// private static final String EXPECTED_OUTPUT_STRING = "{\"FOO\": \"BAR\"}";
//
// @Test
// public void testUserFilterStreamLambda() throws IOException {
// final UserFilterStreamLambda handler = new UserFilterStreamLambda();
// final TestContext ctx = new TestContext();
//
// final InputStream input = new ByteArrayInputStream(SAMPLE_INPUT_STRING.getBytes());
// ;
// final OutputStream output = new ByteArrayOutputStream();
//
// handler.handleRequest(input, output, ctx);
//
// // TODO: validate output here if needed.
// final String sampleOutputString = output.toString();
// System.out.println(sampleOutputString);
// // Assert.assertEquals(EXPECTED_OUTPUT_STRING, sampleOutputString);
// }
// }
