package com.webservices.cloudwebapp.webapp.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


@Configuration
public class AwsS3ClientConfig {

//    @Value("${cloud.aws.credentials.access-key}")
//    private String awsId;
//
//    @Value("${cloud.aws.credentials.secret-key}")
//    private String awsKey;
//    @Value("${cloud.aws.profile}")
//    private String awsProfile;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Bean
    public AmazonS3 s3client() {

//      BasicAWSCredentials awsCredentials = new BasicAWSCredentials(awsId, awsKey);
      AmazonS3 amazonS3Client = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.fromName(region))
//                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
//              .withCredentials(new ProfileCredentialsProvider(awsProfile))
                .build();


        return amazonS3Client;
    }

}
