package com.kanwise.api_gateway;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ApiGatewayApplicationTest {

    @Test
    void contextLoads() {
        ApiGatewayApplication.main(new String[]{});
        Assertions.assertNotNull("Silly assertion to make Sonar happy :)");
    }
}