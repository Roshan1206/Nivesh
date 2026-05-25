package com.nivesh.library;

import com.nivesh.library.exception.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Configuration class for managing all configurations
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
public class NiveshLibraryAutoConfiguration {
}
