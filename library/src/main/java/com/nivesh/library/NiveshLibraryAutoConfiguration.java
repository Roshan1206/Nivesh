package com.nivesh.library;

import com.nivesh.library.properties.CacheProperties;
import com.nivesh.library.exception.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

/**
 * Spring Boot auto-configuration for the Nivesh Library.
 * Automatically scans and registers library components including configurations and services.
 *
 * @author Roshan
 */
@AutoConfiguration
@ComponentScan(
        basePackages = {
                "com.nivesh.library.configuration",
                "com.nivesh.library.service"
        },
        basePackageClasses = {GlobalExceptionHandler.class})
@EnableConfigurationProperties(CacheProperties.class)
public class NiveshLibraryAutoConfiguration {
}
