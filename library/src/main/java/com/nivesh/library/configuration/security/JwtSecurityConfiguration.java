package com.nivesh.library.configuration.security;

import com.nivesh.library.constant.Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Security configuration for nivesh.
 * Manages authentication and authorization using JWT tokens.
 * All routes must be defined here
 *
 * @author Roshan
 */
@Configuration
public class JwtSecurityConfiguration {

    /**
     * Auth server url for decoder
     */
    @Value("${nivesh.auth.url}")
    private String authUrl;

    /**
     * Filter chain for jwt tokens. Creates stateless session.
     * All routes should be secured following their requirements.
     * Uses JWT for OAuth2 server with customized token converter and decoder
     */
    @Order(3)
    @Bean
    public SecurityFilterChain jwtTokenSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .securityMatcher("/**")
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(req -> req
                        .requestMatchers("/auth/**", "/error").permitAll()
                        // Internal endpoints require service-to-service authorization instead of user scopes.
                        .requestMatchers("/*/internal/**").access(new InternalServiceAuthorizationManager())
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter())))
                .csrf(AbstractHttpConfigurer::disable);
        return httpSecurity.build();
    }


    /**
     * Converts claims into roles and scopes for authorization purpose.
     * Used for jwt token
     */
    private Converter<Jwt, ? extends AbstractAuthenticationToken> jwtConverter() {
        JwtGrantedAuthoritiesConverter scopeConverter = new JwtGrantedAuthoritiesConverter();
        scopeConverter.setAuthoritiesClaimName("scope");
        scopeConverter.setAuthorityPrefix("SCOPE_");

        JwtGrantedAuthoritiesConverter roleConverter = new JwtGrantedAuthoritiesConverter();
        roleConverter.setAuthoritiesClaimName("role");
        roleConverter.setAuthorityPrefix("");

        return jwt -> {
            Set<GrantedAuthority> combined = new HashSet<>();
            Collection<GrantedAuthority> scopes = scopeConverter.convert(jwt);
            if (scopes != null) combined.addAll(scopes);
            Collection<GrantedAuthority> roles = roleConverter.convert(jwt);
            if (roles != null) combined.addAll(roles);
            String token_type = jwt.getClaimAsString(Constants.TOKEN_TYPE);
            if (token_type != null) {
                combined.add(new SimpleGrantedAuthority(Constants.TOKEN_TYPE + "_" + token_type));
            }
            return new JwtAuthenticationToken(jwt, combined, jwt.getSubject());
        };
    }


    /**
     * Create JWT decoder. Fetches public key from auth server.
     * Will be created only if the upstream server doesn't have its own decoder.
     */
    @Bean
    @ConditionalOnMissingBean(JwtDecoder.class)
    public JwtDecoder jwtDecoder(){
        // Resource servers validate signatures against the auth server's JWK set endpoint.
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(authUrl + "/oauth2/jwks").build();
        OAuth2TokenValidator<Jwt> validator = JwtValidators.createDefaultWithIssuer(authUrl);
        decoder.setJwtValidator(validator);
        return decoder;
    }


    /**
     * Creates AuthenticationManager for token authentication.
     * {@code JwtAuthenticationProvider} used for validating JWT tokens
     */
    @Bean
    @ConditionalOnMissingBean(AuthenticationManager.class)
    public AuthenticationManager authenticationManager (JwtDecoder jwtDecoder) {
        JwtAuthenticationProvider provider = new JwtAuthenticationProvider(jwtDecoder);
        return new ProviderManager(provider);
    }
}
