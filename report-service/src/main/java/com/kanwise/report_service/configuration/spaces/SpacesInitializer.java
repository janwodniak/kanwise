package com.kanwise.report_service.configuration.spaces;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class SpacesInitializer {

    private final SpacesConfigurationProperties spacesConfigurationProperties;

    @Bean
    public AmazonS3 space() {
        return AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(getEndpointConfiguration())
                .withCredentials(getCredentialsProvider())
                .withPathStyleAccessEnabled(true)
                .build();
    }

    private AwsClientBuilder.EndpointConfiguration getEndpointConfiguration() {
        return new AwsClientBuilder.EndpointConfiguration(
                spacesConfigurationProperties.serviceEndpoint(),
                spacesConfigurationProperties.signingRegion()
        );
    }

    private AWSStaticCredentialsProvider getCredentialsProvider() {
        return new AWSStaticCredentialsProvider(new BasicAWSCredentials(
                spacesConfigurationProperties.accessKey(),
                spacesConfigurationProperties.secretKey()
        ));
    }
}
