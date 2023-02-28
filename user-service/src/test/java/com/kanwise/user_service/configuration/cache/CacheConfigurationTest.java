package com.kanwise.user_service.configuration.cache;

import com.google.common.cache.LoadingCache;
import com.kanwise.user_service.configuration.security.brute_force_attack.BruteForceAttackConfigurationProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = CacheConfiguration.class)
@ActiveProfiles("test")
class CacheConfigurationTest {

    private final CacheConfiguration cacheConfiguration;
    private final ApplicationContext applicationContext;

    @MockBean
    private BruteForceAttackConfigurationProperties bruteForceAttackConfigurationProperties;

    @Autowired
    public CacheConfigurationTest(CacheConfiguration cacheConfiguration, ApplicationContext applicationContext) {
        this.cacheConfiguration = cacheConfiguration;
        this.applicationContext = applicationContext;
    }

    @Test
    void shouldPopulateCache() {
        // Given
        when(bruteForceAttackConfigurationProperties.expireAfterWriteUnit()).thenReturn(15);
        when(bruteForceAttackConfigurationProperties.expireAfterWriteTimeUnit()).thenReturn(MINUTES);
        // When
        LoadingCache<String, Integer> loginAttemptCache = cacheConfiguration.loginAttemptCache();
        Object loginAttemptCacheBean = applicationContext.getBean("loginAttemptCache");
        // Then
        assertNotNull(loginAttemptCache);
        assertNotNull(loginAttemptCache);
        assertEquals(loginAttemptCache, loginAttemptCacheBean);
    }
}