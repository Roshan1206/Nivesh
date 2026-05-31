package com.nivesh.authentication.config.resource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;

import java.security.interfaces.RSAPublicKey;
import java.util.List;

/**
 * Override library Security configuration.
 *
 * @author Roshan
 */
@Configuration
public class SecurityConfiguration {

    /**
     * Jwt decoder for its own. Injecting Public Key to restrain self call
     */
    @Bean
    public JwtDecoder jwtDecoder(RSAPublicKey publicKey) {
        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }


    /**
     * Creates AuthenticationManager for token authentication.
     * {@code DaoAuthenticationProvider} for login while {@code JwtAuthenticationProvider} used for validating JWT tokens
     */
    @Bean
    public AuthenticationManager authenticationManager (UserDetailsService userDetailsService,
                                                        PasswordEncoder passwordEncoder,
                                                        JwtDecoder jwtDecoder) {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);

        JwtAuthenticationProvider jwtAuthenticationProvider = new JwtAuthenticationProvider(jwtDecoder);
        return new ProviderManager(List.of(daoAuthenticationProvider, jwtAuthenticationProvider));
    }
}
