package com.nivesh.library.configuration.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.nivesh.library.cache.properties.OtpCacheProperties;
import com.nivesh.library.constant.CacheConstants;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@EnableCaching
@Configuration
public class CacheConfiguration {

    /**
     * Creates a Caffeine cache manager with TTL and size limits.
     * Automatically evicts expired entries and maintains cache statistics.
     *
     * @return configured CacheManager
     */
    @Bean
    public CacheManager cacheManager() {
//        List<String> caches = List.of(CacheConstants.OTP_CACHE_NAME, CacheConstants.TRANSACTION_CACHE_NAME);
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(CacheConstants.OTP_CACHE_NAME, CacheConstants.TRANSACTION_CACHE_NAME);
        cacheManager.setCaffeine(
                Caffeine.newBuilder()
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .maximumSize(100)
                        .recordStats()
        );
        return cacheManager;
    }
}
