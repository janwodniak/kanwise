package com.kanwise.user_service.configuration.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.kanwise.user_service.configuration.security.brute_force_attack.BruteForceAttackConfigurationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class CacheConfiguration {

    private final BruteForceAttackConfigurationProperties bruteForceAttackConfigurationProperties;

    @Bean
    public LoadingCache<String, Integer> loginAttemptCache() {
        return CacheBuilder.newBuilder().expireAfterWrite(bruteForceAttackConfigurationProperties.expireAfterWriteUnit(), bruteForceAttackConfigurationProperties.expireAfterWriteTimeUnit())
                .maximumSize(100).build(new CacheLoader<>() {
                    @SuppressWarnings("NullableProblems")
                    public Integer load(String key) {
                        return 0;
                    }
                });
    }
}
