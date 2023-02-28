package com.kanwise.api_gateway.configuration.swagger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spring.web.plugins.Docket;

import static springfox.documentation.spi.DocumentationType.SWAGGER_2;

@Configuration
public class SwaggerConfiguration {

    @Bean
    public Docket docket() {
        return new Docket(SWAGGER_2)
                .apiInfo(apiInformation())
                .useDefaultResponseMessages(false)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .paths(e -> !e.endsWith("/error"))
                .build();
    }

    private ApiInfo apiInformation() {
        return new ApiInfoBuilder()
                .title("Kanwise Api Gateway")
                .description("Api Gateway service for Kanwise application")
                .contact(new Contact("Jan Wodniak", "https://github.com/janwodniak", "janwodniak@gmail"))
                .version("1.0")
                .build();
    }
}
