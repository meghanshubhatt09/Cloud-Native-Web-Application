package com.webservices.cloudwebapp.webapp.restController;
import com.timgroup.statsd.StatsDClient;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class AppStatus {
    private final static Logger LOGGER = LoggerFactory.getLogger(AppStatus.class);

    @Autowired
    private StatsDClient metricsClient;
    @RequestMapping(value="/health", method = RequestMethod.GET)
    public ResponseEntity heathCheck(){
        long startTime = System.currentTimeMillis();
        metricsClient.recordExecutionTime("endpoint.healthz.http.GET",startTime);
        LOGGER.info("Application is UP");
        metricsClient.incrementCounter("endpoint.healthz.http.GET");
        return new ResponseEntity(HttpStatus.OK);
    }
}

