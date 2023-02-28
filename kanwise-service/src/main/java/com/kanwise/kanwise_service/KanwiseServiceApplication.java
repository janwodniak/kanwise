package com.kanwise.kanwise_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

@ConfigurationPropertiesScan
@EnableConfigurationProperties
@EnableAsync
@EnableEurekaClient
@EnableFeignClients(basePackages = "com.kanwise.clients")
@SpringBootApplication
public class KanwiseServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(KanwiseServiceApplication.class, args);
    }


}
