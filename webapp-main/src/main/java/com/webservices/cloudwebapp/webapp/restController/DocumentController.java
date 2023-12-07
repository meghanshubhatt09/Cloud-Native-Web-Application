package com.webservices.cloudwebapp.webapp.restController;

import com.timgroup.statsd.StatsDClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.webservices.cloudwebapp.webapp.model.Document;
import com.webservices.cloudwebapp.webapp.model.User;
import com.webservices.cloudwebapp.webapp.repository.DocumentRepository;
import com.webservices.cloudwebapp.webapp.repository.UserRepository;
import com.webservices.cloudwebapp.webapp.service.DocumentService;
import com.webservices.cloudwebapp.webapp.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController // REST APIS
@RequestMapping("/v1")
public class DocumentController {
    private final static Logger LOGGER = LoggerFactory.getLogger(DocumentController.class);
    @Autowired
    private StatsDClient metricsClient;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DocumentService documentService;

    public DocumentController(DocumentService documentService,DocumentRepository documentRepository, UserService userService,UserRepository userRepository) {
        this.documentService = documentService;
        this.documentRepository = documentRepository;

    }

    @GetMapping("/documents")
    public ResponseEntity<?> getListOfFiles(HttpServletRequest request) {
        metricsClient.incrementCounter("endpoint.GetAlldocuments.http.GET");
        String logged = "";
        User user = null;
        String password = "";
        try {
            logged = authenticatedUser(request);
            String loggedUser = logged.split(" ")[0];
            password = logged.split(" ")[1];
            user = userRepository.findUserByUsername(loggedUser);
        }
        catch (Exception e){
            return new ResponseEntity("No Auth in GET", HttpStatus.UNAUTHORIZED);
        }
        if(!passwordEncoder.matches(password, user.getPassword())){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized to access");
        }
        if (user.isVerified() == false)
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized to access because User is not Verified");
        }
        List<Document> myList = documentService.getAllDocument();
        ArrayList<Document> mydocumnets = new ArrayList<>();
        for (int i = 0; i<myList.size(); i++)
        {
            if (user.getId().toString().equals(myList.get(i).getUser_id().toString())){
                mydocumnets.add(myList.get(i));
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(mydocumnets);
    }

    @GetMapping("/documents/{doc_id}")
    public ResponseEntity<?> getDocumentID(@PathVariable("doc_id") UUID doc_id, HttpServletRequest request)
    {

        metricsClient.incrementCounter("endpoint.GetDocumentByID.http.GET");
        Document document = null;

        String logged = "";
        User user = null;
        String password = "";
        UUID userId = null;
        try {
            logged = authenticatedUser(request);
            String loggedUser = logged.split(" ")[0];
            password = logged.split(" ")[1];
            user = userRepository.findUserByUsername(loggedUser);
        }
        catch (Exception e){
            return new ResponseEntity("No Auth in GET", HttpStatus.UNAUTHORIZED);
        }
        if(!passwordEncoder.matches(password, user.getPassword())){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized to access");
        }
        try {
            document = documentRepository.findDocumentByDoc_id(doc_id);
             userId = document.getUser_id();
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Document Not Found");
        }

        if (!user.getId().toString().equals((userId).toString())){
            return new ResponseEntity<>("DENY",HttpStatus.FORBIDDEN);
        }
        if (user.isVerified() == false)
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized to access because User is not Verified");
        }

        List<Document> myList = documentService.getAllDocument();
        ArrayList<Document> mydocumnets = new ArrayList<>();
        for (int i = 0; i<myList.size(); i++)
        {
            if (user.getId().toString().equals(myList.get(i).getUser_id().toString()) &&
                    myList.get(i).getDoc_id().equals(doc_id)){
                mydocumnets.add(myList.get(i));
            }

        }
        if (mydocumnets.isEmpty()){
            ResponseEntity.status(HttpStatus.OK).body("No Data Found");
        }
        return ResponseEntity.status(HttpStatus.OK).body(mydocumnets);
    }

    @DeleteMapping(value = "/documents/{doc_id}")
    public ResponseEntity<?> deleteFile(@PathVariable("doc_id") UUID doc_id, HttpServletRequest request) {
        metricsClient.incrementCounter("endpoint.DeleteDocumentByID.http.DELETE");

        Document document = null;
        String logged = "";
        User user = null;
        String password = "";
        UUID userId = null;
        try {
            logged = authenticatedUser(request);
            String loggedUser = logged.split(" ")[0];
            password = logged.split(" ")[1];
            user = userRepository.findUserByUsername(loggedUser);
        }
        catch (Exception e){
            LOGGER.warn("Document Failed to Delete");
            return new ResponseEntity("No Auth in GET", HttpStatus.UNAUTHORIZED);
        }

        if(!passwordEncoder.matches(password, user.getPassword())){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized to access");
        }
        if (user.isVerified() == false)
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized to access because User is not Verified");
        }

        try {
            LOGGER.warn("Document Failed to Delete");
            document = documentRepository.findDocumentByDoc_id(doc_id);
            userId = document.getUser_id();
        }
        catch (Exception e)
        {
            LOGGER.warn("Document Failed to Delete");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Document Not Found");
        }


        if (!user.getId().toString().equals((userId).toString())){
            LOGGER.warn("Document Failed to Delete");
            return new ResponseEntity<>("DENY",HttpStatus.FORBIDDEN);
        }

        if (document == null){
            LOGGER.warn("No Document Found to Delete");
            return new ResponseEntity<>("No Record Found",HttpStatus.NOT_FOUND);
        }


        boolean isRemoved = documentService.deleteFile(doc_id);
        LOGGER.info("Document Successful Deleted");
        return new ResponseEntity<>(doc_id, HttpStatus.OK);

    }


    @PostMapping("/documents")
    public ResponseEntity uploadFile(HttpServletRequest request,@RequestParam("fileName") String fileName,
                                        @RequestParam("file") MultipartFile file)
    {
        metricsClient.incrementCounter("endpoint.CreateDocument.http.POST");
        String logged = "";

        try {
            logged = authenticatedUser(request);
        }
        catch (Exception e){
            LOGGER.info("Document Failed Uploaded");
            return new ResponseEntity("No Auth in GET", HttpStatus.UNAUTHORIZED);
        }
        String fileNameEdited =  file.getOriginalFilename() + "-" + System.currentTimeMillis();
        String loggedUser = logged.split(" ")[0];
        String password = logged.split(" ")[1];
        User user = userRepository.findUserByUsername(loggedUser);

        LOGGER.info("Document Successful Uploaded");
        if (user.isVerified() == false)
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized to access because User is not Verified");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(documentService.uploadFile(fileNameEdited, file,loggedUser));
    }

//    @GetMapping(value = "/delete/{filename}")
//    public ResponseEntity<String> deleteFile(@PathVariable("filename") String filename) {
//        return new ResponseEntity<>(service.deleteFile(filename), HttpStatus.OK);
//    }



    private MediaType contentType(String filename) {
        String[] fileArrSplit = filename.split("\\.");
        String fileExtension = fileArrSplit[fileArrSplit.length - 1];
        switch (fileExtension) {
            case "txt":
                return MediaType.TEXT_PLAIN;
            case "png":
                return MediaType.IMAGE_PNG;
            case "jpg":
                return MediaType.IMAGE_JPEG;
            default:
                return MediaType.APPLICATION_OCTET_STREAM;
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

}
