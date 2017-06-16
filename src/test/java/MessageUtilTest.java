import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MessageUtilTest {

    String message = "Robert";
    MessageUtil messageUtil = new MessageUtil();

    @Test
    public void testPrintMessage() {
        System.out.println("Inside testPrintMessage()");
        messageUtil.salutationMessage(message);
        assertEquals("Hi! Robert", messageUtil.printMessage());
    }

    @Test
    public void testSalutationMessage() {
        System.out.println("Inside testSalutationMessage()");
        message = "Robert";
        assertEquals("Hi! Robert", messageUtil.salutationMessage(message));
    }
}