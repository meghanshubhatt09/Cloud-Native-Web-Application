package com.webservices.cloudwebapp.webapp.restController;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.timgroup.statsd.StatsDClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.webservices.cloudwebapp.webapp.model.User;
import com.webservices.cloudwebapp.webapp.repository.UserRepository;
import com.webservices.cloudwebapp.webapp.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.amazonaws.services.sns.model.*;


@RestController // REST APIS
@RequestMapping("/v1")
public class UserController {

    private final static Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private StatsDClient metricsClient;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public UserController(UserService userService,UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;

    }

    @GetMapping("/testload")
    public int getAllRecords() throws Exception
    {
        return userService.getAllUser().size();
    }
    @GetMapping("account/{id}")
    public ResponseEntity<?> getUserById(@PathVariable("id") UUID id, HttpServletRequest request) {
        metricsClient.incrementCounter("endpoint.accountByID.http.GET");

        String logged = "";
        Optional<User> user = null;

        try {
            logged = authenticatedUser(request);
            user = userRepository.findById(id);
        }
        catch (Exception e){
            return new ResponseEntity("No Auth in GET", HttpStatus.UNAUTHORIZED);
        }

        String loggedUser = logged.split(" ")[0];
        String password = logged.split(" ")[1];


        if(user.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        if(!user.get().getUsername().equals(loggedUser)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized to access");
        }

        if(!passwordEncoder.matches(password, user.get().getPassword())){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized to access");
        }

        if (user.get().isVerified() == false){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized to access because User is not Verified");
        }

        return ResponseEntity.status(HttpStatus.OK).body(user);
    }
    @PostMapping("/account")
    public ResponseEntity createAccount(@RequestBody User userDetail, HttpServletRequest request,HttpServletResponse response) throws Exception
    {
        long startTime = System.currentTimeMillis();
        metricsClient.recordExecutionTime("endpoint.createAccount.http.POST",startTime);
        metricsClient.incrementCounter("endpoint.createAccount.http.POST");
        // Username needs to be an email address and unique or else throw BAD REQUEST
        if (userDetail.getUsername() == null || userDetail.getFirst_name() == null || userDetail.getLast_name() == null
                || userDetail.getPassword() == null ) {
            LOGGER.warn("User Registration Failed");
            return new ResponseEntity("Null value Bad Request", HttpStatus.BAD_REQUEST);
        }
        if (userDetail.getId() != null || userDetail.getAccount_created() != null
                || userDetail.getAccount_updated() != null)
        {
            LOGGER.warn("User Registration Failed");
            return new ResponseEntity("Bad Request", HttpStatus.BAD_REQUEST);
        }


        if (userService.isEmailVaild(userDetail.getUsername()) == false)
        {
            LOGGER.warn("User Registration Failed");
            return new ResponseEntity("Correct Email Required",HttpStatus.BAD_REQUEST);
        }

        if (userRepository.findByEmail(userDetail.getUsername().toString())){
            LOGGER.warn("User Registration Failed");
            return new ResponseEntity("Username, Already exist !",HttpStatus.BAD_REQUEST);
        }

        LOGGER.info("User Registration Successful");

        AmazonSNS snsClient = AmazonSNSClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
        CreateTopicResult topicResult = snsClient.createTopic("email");
        String topicArn = topicResult.getTopicArn();
        final PublishRequest publishRequest = new PublishRequest(topicArn, userDetail.getUsername());
        LOGGER.warn("Verfication reset request made"+publishRequest.getMessage());
        final PublishResult publishResponse = snsClient.publish(publishRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUser(userDetail));
    }
    @PutMapping("/account/{id}")
    public ResponseEntity updateUser(@PathVariable("id") UUID id, @RequestBody User userDetail,
                                     HttpServletRequest request) throws Exception
    {
        metricsClient.incrementCounter("endpoint.updateAccount.http.PUT");
        String logged = "";
        Optional<User> updateThisUser = null;

        try {
            logged = authenticatedUser(request);
            updateThisUser = userRepository.findById(id);
        }
        catch (Exception e){
            LOGGER.warn("User Update Failed");
            return new ResponseEntity("No Auth in PUT", HttpStatus.UNAUTHORIZED);
        }

          String loggedUser = logged.split(" ")[0];
          String password = logged.split(" ")[1];


        if(!updateThisUser.get().getUsername().equals(loggedUser)){
            LOGGER.warn("User Update Failed");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized to access");
        }

        if(!passwordEncoder.matches(password, updateThisUser.get().getPassword())){
            LOGGER.warn("User Update Failed");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized to access");
        }

          if (userDetail.getId() == null & userDetail.getUsername() == null & userDetail.getFirst_name() == null
          & userDetail.getLast_name() == null
                  & userDetail.getAccount_created() == null & userDetail.getAccount_updated() == null)
          {
              LOGGER.warn("User Update Failed");
              return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Request body cannot be empty");
          }

          if (userDetail.getId()!=null) {
              if (!updateThisUser.get().getId().toString().equals(userDetail.getId().toString())) {
                  LOGGER.warn("User Update Failed");
                  return new ResponseEntity("Bad Request", HttpStatus.BAD_REQUEST);
              }
          }

          if (userDetail.getAccount_created() != null || userDetail.getAccount_updated() != null)
          {
              LOGGER.warn("User Update Failed");
              return new ResponseEntity("Bad Request", HttpStatus.BAD_REQUEST);
          }

          if (updateThisUser.isEmpty())
          {
              LOGGER.warn("User Update Failed");
              return new ResponseEntity("No Content",HttpStatus.NO_CONTENT);
          }
          if (userDetail.getUsername()!=null ) {
              if (!userDetail.getUsername().equals(updateThisUser.get().getUsername()) & (!userDetail.getUsername().isEmpty())) {
                  LOGGER.warn("User Update Failed");
                  return new ResponseEntity("Bad Request", HttpStatus.BAD_REQUEST);
              }
          }
        if (userDetail.isVerified() == false)
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized to access because User is not Verified");
        }

          LOGGER.info("User Update Successful");
          return userService.updateUser(updateThisUser,userDetail);
    }

    @GetMapping("/verifyUserEmail")
    public ResponseEntity verifyUser(@RequestParam String email, @RequestParam String token) throws Exception
    {
        metricsClient.incrementCounter("endpoint./verifyUserEmail.http.get");
        if(email==null || token==null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\": \"token not found\"}");
        User user = userRepository.findUserByUsername(email);

        if (user.isVerified() == false) {
            if (validToken(user.getUsername()) == true) {
                user.setVerified(true);
                userRepository.save(user);
                return ResponseEntity.status(HttpStatus.OK).body("You are now Verified !");
            }
            return ResponseEntity.status(HttpStatus.OK).body("Link Expired :( !");

        }
        else{
            if (user.isVerified() == true && validToken(user.getUsername()) == true) {
                return ResponseEntity.status(HttpStatus.OK).body("User is already Verified !");
            }
            else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Link Expired :( !");
            }
        }
    }



    private String authenticatedUser(HttpServletRequest request){

        String tokenEnc = request.getHeader("Authorization").split(" ")[1];
        byte[] token = Base64.getDecoder().decode(tokenEnc);
        String decodedStr = new String(token, StandardCharsets.UTF_8);

        String userName = decodedStr.split(":")[0];
        String passWord = decodedStr.split(":")[1];
        System.out.println("Value of Token" + " "+ decodedStr);

        return userName +" "+ passWord;

    }



    private boolean validToken(String emailId){

        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withCredentials(new DefaultAWSCredentialsProviderChain()).build();
        DynamoDB dynamoDB = new DynamoDB(client);
        Table table = dynamoDB.getTable("csye6225");

        long currentEpochTime= System.currentTimeMillis() / 1000L;
        GetItemSpec spec = new GetItemSpec().
                withPrimaryKey("emailId", emailId)
                .withProjectionExpression("ExpirationTime")
                .withConsistentRead(true);
        Item item = table.getItem(spec);

        try {
            if (Objects.nonNull(item) )
            {
                Long expirationTime = Long.parseLong(item.getString("ExpirationTime"));
                if (currentEpochTime < expirationTime )
                {
                    LOGGER.info("Details Found");
                    return true;
                }
                else {
                    return false;
                }

            }
        }
        catch (Exception e)
        {
            LOGGER.error("Exception occurred request denied (Session Expired) : ", e);
        }
        return false;

    }
}



// update firstname, lastname and password
