// package com.amazonaws.lambda.userfilterlambda;
//
// import static org.junit.Assert.assertEquals;
// import static org.junit.Assert.assertTrue;
// import static org.junit.Assert.fail;
// import static org.mockito.Matchers.any;
// import static org.mockito.Mockito.when;
// import static org.powermock.api.mockito.PowerMockito.mock;
//
// import java.io.IOException;
//
// import org.junit.Before;
// import org.junit.BeforeClass;
// import org.junit.Test;
// import org.junit.runner.RunWith;
// import org.mockito.Matchers;
// import org.mockito.Mockito;
// import org.powermock.api.mockito.PowerMockito;
// import org.powermock.core.classloader.annotations.PrepareForTest;
// import org.powermock.modules.junit4.PowerMockRunner;
//
// import com.amazonaws.regions.Regions;
// import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
// import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
// import com.amazonaws.services.dynamodbv2.document.DynamoDB;
// import com.amazonaws.services.dynamodbv2.document.Item;
// import com.amazonaws.services.dynamodbv2.document.ItemCollection;
// import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
// import com.amazonaws.services.dynamodbv2.document.Table;
// import com.amazonaws.services.dynamodbv2.document.internal.IteratorSupport;
// import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
// import com.amazonaws.services.lambda.runtime.Context;
// import com.amazonaws.services.sns.AmazonSNS;
// import com.amazonaws.services.sns.AmazonSNSClientBuilder;
// import com.amazonaws.services.sns.model.InternalErrorException;
// import com.amazonaws.services.sns.model.PublishRequest;
//
/// **
// * A simple test harness for locally invoking your Lambda function handler.
// */
// @RunWith(PowerMockRunner.class)
// @PrepareForTest({ UserFilterLambda.class, AmazonDynamoDBClientBuilder.class, DynamoDB.class,
// AmazonSNSClientBuilder.class })
// public class UserFilterLambdaTest {
// private final String TABLE_NAME = "PNC_SUBSCRIBER";
// private final String COL_PARTITION_KEY = "TENANTID_PERSONID";// "TENANT_PERSONID_BYTE";
// private final String COL_SORT_KEY = "TOPIC";
// private final Regions REGION = Regions.US_WEST_2;
//
// private static PushToken input;
// private Table mockTable;
// private AmazonSNS mockSNSClient;
//
// @BeforeClass
// public static void createInput() throws IOException {
//
// // set up sample input object here.
// input = new PushToken();
// input.setTenantKey("dev");
// input.setUserId(1234);
// input.setMessage("test");
// input.setTopic("cosign");
// }
//
// @Before
// public void setup() throws Exception {
// setupMock();
// }
//
// private void setupMock() throws Exception {
// mockDynamoDB();
// mockSNS();
// }
//
// private void mockDynamoDB() throws Exception {
// PowerMockito.mockStatic(AmazonDynamoDBClientBuilder.class);
// final AmazonDynamoDB mockClient = mock(AmazonDynamoDB.class);
// final AmazonDynamoDBClientBuilder mockBuilder = mock(AmazonDynamoDBClientBuilder.class);
// when(AmazonDynamoDBClientBuilder.standard()).thenReturn(mockBuilder);
// when(mockBuilder.withRegion(Regions.US_WEST_2)).thenReturn(mockBuilder);
// when(mockBuilder.build()).thenReturn(mockClient);
//
// final DynamoDB mockDB = mock(DynamoDB.class);
// mockTable = mock(Table.class);
// PowerMockito.whenNew(DynamoDB.class).withArguments(mockClient).thenReturn(mockDB);
// PowerMockito.when(mockDB.getTable(Matchers.anyString())).thenReturn(mockTable);
// }
//
// private void mockSNS() {
// PowerMockito.mockStatic(AmazonSNSClientBuilder.class);
// final AmazonSNSClientBuilder mockBuilder = mock(AmazonSNSClientBuilder.class);
// mockSNSClient = mock(AmazonSNS.class);
// when(AmazonSNSClientBuilder.standard()).thenReturn(mockBuilder);
// when(mockBuilder.withRegion(REGION)).thenReturn(mockBuilder);
// when(mockBuilder.build()).thenReturn(mockSNSClient);
// }
//
// private Context createContext() {
// final TestContext ctx = new TestContext();
//
// // customize context here if needed.
// ctx.setFunctionName("User Filter Lambda");
//
// return ctx;
// }
//
// private Item createItem() {
// final Item item = new Item();
// item.withBinary(COL_PARTITION_KEY, "/dev/1234".getBytes());
// item.withString(COL_SORT_KEY, "cosign");
// item.withString("INFO", "this is a test item");
// item.withString("TOPIC_ARN", "aws:topic:push_notification");
//
// return item;
// }
//
// @Test
// public void testUserFilterHandler_WithSubscribedUser() {
//
// final UserFilterLambda handler = new UserFilterLambda();
// final Context ctx = createContext();
//
// // mock return value from dynamoDB table
// final ItemCollection<QueryOutcome> mockOutcome = mock(ItemCollection.class);
// when(mockTable.query(any(QuerySpec.class))).thenReturn(mockOutcome);
//
// final IteratorSupport<Item, QueryOutcome> mockIterator = mock(IteratorSupport.class);
// final Item expectedItem = createItem();
//
// when(mockOutcome.iterator()).thenReturn(mockIterator);
// when(mockIterator.hasNext()).thenReturn(true, false);
// when(mockIterator.next()).thenReturn(expectedItem);
// System.out.println("item:" + expectedItem.getString(COL_SORT_KEY));
//
// // mock sns
// when(mockSNSClient.publish(Mockito.any(PublishRequest.class))).thenReturn(null);
//
// final String output = handler.handleRequest(input, ctx);
//
// // validate output
// Mockito.verify(mockSNSClient, Mockito.times(1)).publish(Mockito.any(PublishRequest.class));
// if (output != null) {
// assertEquals(output, "tenantId_personId: /dev/1234, topic: cosign, message: aws:topic:push_notification");
// }
// }
//
// @Test
// public void UserFilterHandler_WithUnSubscribedUser() {
//
// final UserFilterLambda handler = new UserFilterLambda();
// final Context ctx = createContext();
//
// // mock return value from dynamoDB table
// final ItemCollection<QueryOutcome> mockOutcome = mock(ItemCollection.class);
// when(mockTable.query(any(QuerySpec.class))).thenReturn(mockOutcome);
//
// final IteratorSupport<Item, QueryOutcome> mockIterator = mock(IteratorSupport.class);
// final Item expectedItem = createItem();
//
// when(mockOutcome.iterator()).thenReturn(mockIterator);
// when(mockIterator.hasNext()).thenReturn(false);
//
// // mock sns
// when(mockSNSClient.publish(Mockito.any(PublishRequest.class))).thenReturn(null);
// final String output = handler.handleRequest(input, ctx);
//
// // validate output
// Mockito.verify(mockSNSClient, Mockito.times(0)).publish(Mockito.any(PublishRequest.class));
// assertTrue(output.isEmpty());
// }
//
// @Test
// public void UserFilterHandler_Fail() {
//
// final UserFilterLambda handler = new UserFilterLambda();
// final Context ctx = createContext();
//
// // mock return value from dynamoDB table
// final ItemCollection<QueryOutcome> mockOutcome = mock(ItemCollection.class);
// when(mockTable.query(any(QuerySpec.class))).thenReturn(mockOutcome);
//
// final IteratorSupport<Item, QueryOutcome> mockIterator = mock(IteratorSupport.class);
// final Item expectedItem = createItem();
//
// when(mockOutcome.iterator()).thenReturn(mockIterator);
// when(mockIterator.hasNext()).thenReturn(true, false);
// when(mockIterator.next()).thenReturn(expectedItem);
// System.out.println("item:" + expectedItem.getString(COL_SORT_KEY));
//
// // mock sns
// when(mockSNSClient.publish(Mockito.any(PublishRequest.class)))
// .thenThrow(new InternalErrorException("fail publish to sns"));
//
// try {
// final String output = handler.handleRequest(input, ctx);
// fail("expected exception was not occured.");
// } catch (final Exception ex) {
// Mockito.verify(mockSNSClient, Mockito.times(1)).publish(Mockito.any(PublishRequest.class));
// System.out.println(ex.getMessage());
// assertTrue(ex.getMessage().contains("Errors during user filtering process"));
// assertTrue(ex.getCause().getMessage().contains("fail publish to sns"));
//
// }
// }
// }
