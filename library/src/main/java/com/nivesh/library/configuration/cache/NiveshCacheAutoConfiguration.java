
package com.nivesh.library.configuration.cache;

import com.nivesh.library.properties.CacheProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class that wires cache configuration settings for the library module.
 */
@EnableCaching
@AutoConfiguration
@EnableConfigurationProperties(CacheProperties.class)
@ConditionalOnClass(RedisConnectionFactory.class)
@ConditionalOnMissingBean(CacheManager.class)
public class NiveshCacheAutoConfiguration {

    /**
     * Default caches
     */
    private static final Map<String, Duration> DEFAULT_CACHE = Map.of(
            "otp", Duration.ofMinutes(5),
            "login-attempt", Duration.ofMinutes(15),
            "jwt-blacklist", Duration.ofMinutes(30),
            "idempotency-key", Duration.ofHours(24),
            "account-balance", Duration.ofSeconds(30),
            "customer-profile", Duration.ofMinutes(10),
            "transaction", Duration.ofMinutes(30),
            "reset-password", Duration.ofMinutes(30)
    );

    /**
     * Creates a Redis cache manager with default Caches.
     * New Caches can be added via application.yml
     * Automatically evicts expired entries and maintains cache statistics.
     *
     * @return configured RedisCacheManager
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory, CacheProperties properties) {
        Map<String, Duration> merged = new HashMap<>(DEFAULT_CACHE);
        merged.putAll(properties.getTtl());

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        merged.forEach((name, ttl) -> {
            RedisCacheConfiguration config = buildCacheConfig(name, ttl);
            cacheConfigs.put(name, config);
        });

        RedisCacheConfiguration config = RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(properties.getDefaultTtl())
                .serializeKeysWith(keySerializer())
                .serializeValuesWith(valueSerializer())
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .withInitialCacheConfigurations(cacheConfigs)
                .transactionAware().build();
    }

    /**
     * Build a cache config for each cache
     */
    private RedisCacheConfiguration buildCacheConfig(String name, Duration ttl) {
        return RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(ttl)
                .serializeKeysWith(keySerializer())
                .serializeValuesWith(valueSerializer())
                .disableCachingNullValues();
    }


    /**
     * Creates key serializer for caches. Stores key in plain text
     */
    private RedisSerializationContext.SerializationPair<String> keySerializer() {
        return RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer());
    }


    /**
     * Creates value serializer for caches. Stores value in JSON format.
     */
    private RedisSerializationContext.SerializationPair<Object> valueSerializer() {
        return RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer());
    }
}
