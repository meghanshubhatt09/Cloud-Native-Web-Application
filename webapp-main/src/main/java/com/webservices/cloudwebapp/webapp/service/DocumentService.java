package com.webservices.cloudwebapp.webapp.service;


import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.webservices.cloudwebapp.webapp.config.AwsS3ClientConfig;
import com.webservices.cloudwebapp.webapp.model.Document;
import com.webservices.cloudwebapp.webapp.model.User;
import com.webservices.cloudwebapp.webapp.repository.DocumentRepository;
import com.webservices.cloudwebapp.webapp.repository.UserRepository;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
public class DocumentService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private AmazonS3 amazonS3Client;

    @Value("${app.awsServices.bucketName}")
    private String bucketName;

    public Document pathDocument(String keyName, String username, URL url) {

        Document documentDetail = new Document();
        User user = userRepository.findUserByUsername(username);
        documentDetail.setUser_id(user.getId());
        documentDetail.setName(keyName);
        documentDetail.setDate_created(LocalDateTime.now());
        documentDetail.setS3_bucket_path(String.valueOf(url));
        documentRepository.save(documentDetail);
        return documentDetail;

    }

    public List<Document> getAllDocument() {
        return (List<Document>) documentRepository.findAll();
    }


    private Logger logger = LoggerFactory.getLogger(DocumentService.class);



    public Document uploadFile(String keyName, MultipartFile file, String loggedUser) {

        try {
            //amazonS3Client = AmazonS3ClientBuilder.defaultClient();
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            amazonS3Client.putObject(bucketName, keyName, file.getInputStream(), metadata);
            return pathDocument(keyName,loggedUser, amazonS3Client.getUrl(bucketName,keyName));

        } catch (IOException ioe) {
            logger.error("IOException: " + ioe.getMessage());
        } catch (AmazonServiceException serviceException) {
            logger.info("AmazonServiceException: "+ serviceException.getMessage());
            throw serviceException;
        } catch (AmazonClientException clientException) {
            logger.info("AmazonClientException Message: " + clientException.getMessage());
            throw clientException;
        }
        return new Document();
    }

    public boolean deleteFile(UUID doc_id) {

        Document doc = documentRepository.findDocumentByDoc_id(doc_id);
        String fileName = doc.getName();
        amazonS3Client.deleteObject(bucketName, fileName);
        documentRepository.deleteByDoc_id(doc_id);
        return true;
    }

    public List<String> listFiles() {

        ListObjectsRequest listObjectsRequest =
                new ListObjectsRequest()
                        .withBucketName(bucketName);

        List<String> keys = new ArrayList<>();

        ObjectListing objects = amazonS3Client.listObjects(listObjectsRequest);

        while (true) {
            List<S3ObjectSummary> objectSummaries = objects.getObjectSummaries();
            if (objectSummaries.size() < 1) {
                break;
            }

            for (S3ObjectSummary item : objectSummaries) {
                if (!item.getKey().endsWith("/"))
                    keys.add(item.getKey());
            }

            objects = amazonS3Client.listNextBatchOfObjects(objects);
        }

        return keys;
    }


}
