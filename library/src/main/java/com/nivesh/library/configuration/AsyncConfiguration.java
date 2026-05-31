package com.nivesh.library.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@EnableAsync
public class AsyncConfiguration {

    @Bean(name = "emailTaskExecutor")
    public Executor emailTaskExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
