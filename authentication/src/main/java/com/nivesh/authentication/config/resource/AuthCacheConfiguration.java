package com.nivesh.authentication.config.resource;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.nivesh.authentication.config.properties.AuthCacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
@EnableConfigurationProperties(AuthCacheProperties.class)
public class AuthCacheConfiguration {

    public static final String REGISTER_CACHE_NAME = "register";
    public static final String LOGIN_CACHE_NAME = "login";

    @Primary
    @Bean({"authCacheManager"})
    public CacheManager authCacheManager(AuthCacheProperties properties) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(REGISTER_CACHE_NAME, LOGIN_CACHE_NAME);
        cacheManager.setCaffeine(
                Caffeine.newBuilder()
                    .maximumSize(properties.getMaxCacheSize())
                    .expireAfterWrite(properties.getLockDurationMin(), TimeUnit.MINUTES)
                    .recordStats()
        );
        return cacheManager;
    }
}
