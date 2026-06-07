package com.nivesh.transaction.config.client;

import com.nivesh.library.configuration.builder.BaseCommunicationClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Client abstraction for communicating with dependent services from the transaction module.
 */
@Configuration
public class AccountCommunication {

    /** Accounts url value used by this component. */
    @Value("${nivesh.account.url}")
    private String accountsUrl;


    /**
     * Creates a new WebClient instance for account-related communication.
     *
     * @param client A BaseCommunicationClient to use as the underlying web client.
     * @return A WebClient instance configured for account operations.
     */
    @Bean
    public WebClient accountClient(BaseCommunicationClient client) {
        return client.create(accountsUrl);
    }
}
