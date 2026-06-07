package com.nivesh.gateway.configuration.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

@Configuration
public class NiveshSecurityConfiguration {

    @Value("${nivesh.auth.url}")
    private String authUrl;


    /**
     * Configures the security web filter chain for the application.
     *
     * @param httpSecurity The HttpSecurity object to configure the filter chain with.
     * @return The SecurityWebFilterChain instance configured by this method.
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity httpSecurity){
        httpSecurity
                .securityMatcher(ServerWebExchangeMatchers.pathMatchers("/**"))
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/auth/**").permitAll()
                        .anyExchange().authenticated())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()));
        return httpSecurity.build();
    }


    /**
     * Creates a ReactiveJwtDecoder for decoding JWT tokens.
     *
     * @return reactiveJwtDecoder
     */
    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        NimbusReactiveJwtDecoder decoder = ReactiveJwtDecoders.fromIssuerLocation(authUrl);
        OAuth2TokenValidator<Jwt> validator = JwtValidators.createDefaultWithIssuer(authUrl);
        decoder.setJwtValidator(validator);
        return decoder;
    }
}
