package com.nivesh.library.configuration.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for actuator endpoints.
 * Creates in-memory user with basic auth.
 *
 * @author Roshan
 */
@Configuration
public class ActuatorSecurityConfiguration {

    /**
     * Responsible for getting username and password
     */
    private final SecurityProperties securityProperties;

    /**
     * Injecting required dependency via Constructor Injection
     */
    public ActuatorSecurityConfiguration(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    /**
     * Filter chain for actuator endpoints.
     * Other than {@code  /health} and {@code /info}, all endpoints required authentication.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .securityMatcher("/actuator/**")
                .authorizeHttpRequests(req -> req
                        .requestMatchers(EndpointRequest.to(HealthEndpoint.class, InfoEndpoint.class)).permitAll()
                        .anyRequest().authenticated())
                .csrf(AbstractHttpConfigurer::disable)
                .authenticationManager(new ProviderManager(new DaoAuthenticationProvider(userDetailsService())))
                .httpBasic(Customizer.withDefaults());
        return httpSecurity.build();
    }

    /**
     * Build in-memory user for actuator endpoints.
     * Should only be used for {@code actuatorSecurityFilterChain}
     */
    private UserDetailsService userDetailsService() {
        UserDetails user = User.withUsername(securityProperties.getUser().getName())
                .password("{noop}" + securityProperties.getUser().getPassword())
                .roles("ACTUATOR")
                .build();
        return new InMemoryUserDetailsManager(user);
    }
}
