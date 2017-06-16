import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class MessageUtil implements RequestHandler<Message, String> {
    private String message;

    public MessageUtil() {
        this.message = message;
    }

    public String printMessage() {
        System.out.println(message);
        return message;
    }

    public String salutationMessage(final String message) {
        this.message = "Hi! " + message;
        System.out.println(this.message);
        return this.message;
    }

    /**
     * {@inheritDoc}
     */
    public String handleRequest(final Message input, final Context context) {
        context.getLogger().log("Input: " + input);
        final MessageUtil messageUtil = new MessageUtil();
        messageUtil.salutationMessage(input.getMessage());
        System.out.println(input.getMessage());
        return messageUtil.message;
    }
}