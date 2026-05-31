package com.nivesh.library.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Configures async processing using virtual threads for improved performance.
 * Enables async task execution with lightweight thread pooling.
 */
@Configuration
@EnableAsync
public class AsyncConfiguration {

    /**
     * Creates an executor that uses virtual threads for email tasks.
     * Each task gets its own virtual thread, reducing thread pool overhead.
     *
     * @return Executor using virtual threads per task
     */
    @Bean(name = "emailTaskExecutor")
    public Executor emailTaskExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
