package com.nivesh.gateway.configuration.security;

import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

@Configuration
public class ActuatorSecurityConfig {

    /**
     * Injecting SecurityProperties to extract username and password
     */
    private final SecurityProperties securityProperties;


    /**
     * Injecting required dependency via Constructor injection.
     */
    public ActuatorSecurityConfig(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }


    /**
     * Actuator filter chain for customizing actuator endpoints
     */
    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity httpSecurity) {
        httpSecurity
                .securityMatcher(ServerWebExchangeMatchers.pathMatchers("/actuator/**"))
                .authorizeExchange(exchange -> exchange
//                        .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                        .anyExchange().permitAll())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authenticationManager(new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService()));
        return httpSecurity.build();
    }


    /**
     * Defining custom user details for actuator endpoints
     */
    public MapReactiveUserDetailsService userDetailsService() {
        UserDetails userDetails = User
                .withUsername(securityProperties.getUser().getName())
                .password("{noop}" + securityProperties.getUser().getPassword())
                .roles("ACTUATOR")
                .build();
        return new MapReactiveUserDetailsService(userDetails);
    }
}