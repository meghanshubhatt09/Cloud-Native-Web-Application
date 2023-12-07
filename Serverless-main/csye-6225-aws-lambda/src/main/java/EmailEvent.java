import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.UUID;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;

public class EmailEvent implements RequestHandler<SNSEvent, Object> {
    public Object handleRequest(SNSEvent request, Context context) {
        //domain and fromemail has to taken from environment variable

        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());
        context.getLogger().log("Invocation started: " + timeStamp);

        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withCredentials(new DefaultAWSCredentialsProviderChain()).build();
        DynamoDB dynamoDB = new DynamoDB(client);
        Table table = dynamoDB.getTable("csye6225");

        UUID token = UUID.randomUUID();
        String toEmail= request.getRecords().get(0).getSNS().getMessage();


        long currentEpochTime= System.currentTimeMillis() / 1000L;

        context.getLogger().log("Current epcho time:" + currentEpochTime);

        long ttl = Integer.parseInt(System.getenv("ttl")) * 60;

        context.getLogger().log("TTL:" + ttl);

        long expirationTime = currentEpochTime + ttl;

        context.getLogger().log("Expiration epcho time:" + expirationTime);

        String domain=System.getenv("domain");

        context.getLogger().log("Domain :" +domain);

        String fromEmail=System.getenv("fromemail");

        context.getLogger().log(toEmail);


        boolean status= sendEmail(domain, fromEmail, toEmail, String.valueOf(token), context);
        if(status){
            putItem(table, toEmail, String.valueOf(token), expirationTime);
        }
            //context.getLogger().log(outcome.getItem().toJSONPretty());


        timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());
        context.getLogger().log("Invocation completed: " + timeStamp);
        return null;
    }

    private boolean sendEmail(String domain, String fromEmail, String toEmail, String token, Context context){
        try {
// http://prod.domain.tld/v1/verifyUserEmail?email=user@example.com&token=sometoken Links to an external site.
            String body = "Hello User, Please Verify the Link";
            String TEXTBODY="https://"+domain +"/v1/verifyUserEmail?email="+ toEmail + "&token=" + token;
            String HTMLBODY="<p>"+ body + "<p>" + TEXTBODY+"<p>";

            AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard().withCredentials(new DefaultAWSCredentialsProviderChain()).build();

            SendEmailRequest request = new SendEmailRequest()
                    .withDestination(
                            new Destination().withToAddresses(toEmail))
                    .withMessage(new Message()
                            .withBody(new Body()
                                    .withHtml(new Content()
                                            .withCharset("UTF-8").withData(HTMLBODY))
                                    .withText(new Content()
                                            .withCharset("UTF-8").withData(TEXTBODY)))
                            .withSubject(new Content()
                                    .withCharset("UTF-8").withData("Verfiy User")))
                    .withSource(fromEmail);
            client.sendEmail(request);
            context.getLogger().log("Email sent!");
            return true;
        } catch (Exception ex) {
            context.getLogger().log("The email was not sent. Error message: "
                    + ex.getMessage());
            return false;
        }
    }

    private void putItem(Table table, String toEmail, String token, long expirationTime){
        Item item = new Item()
                .withPrimaryKey("emailId", toEmail)
                .withString("Token", String.valueOf(token))
                .withNumber("ExpirationTime", expirationTime);

        PutItemOutcome outcome = table.putItem(item);
    }

}

