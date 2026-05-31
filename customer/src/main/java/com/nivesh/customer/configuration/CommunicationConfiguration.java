package com.nivesh.customer.configuration;

import com.nivesh.library.configuration.builder.BaseCommunicationClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class CommunicationConfiguration {

    @Value("${nivesh.auth.url}")
    private String authUrl;


    @Bean(name = "authClient")
    public WebClient createAuthClient(BaseCommunicationClient builder) {
        return builder.create(authUrl);
    }
}
