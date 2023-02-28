package com.kanwise.api_gateway.configuration.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;

import java.util.Objects;

import static java.lang.Boolean.TRUE;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class GatewayConfigurationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void shouldPopulateRouteLocator() {
        // Given
        // When
        RouteLocator routeLocatorBean = applicationContext.getBean(RouteLocator.class);
        Flux<Route> routes = routeLocatorBean.getRoutes();
        // Then
        assertNotNull(routeLocatorBean);
        assertEquals(3, routes.count().block());
        for (String uri : asList("lb://user-service", "lb://kanwise-service", "lb://report-service")) {
            assertEquals(TRUE, routes.any(route -> Objects.equals(route.getUri().toString(), uri)).block());
        }
    }
}