package com.nivesh.library.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration properties needed for Cache.
 * Default value can be overridden via yml file.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "nivesh.cache")
public class CacheProperties {

    /**
     * Per Cache duration override
     * Key = Cache name, Value = Cache duration
     */
    private Map<String, Duration> ttl = new HashMap<>();

    /**
     * if cache has no duration defined than, default will be applied
     */
    private Duration defaultTtl = Duration.ofMinutes(10);
}
