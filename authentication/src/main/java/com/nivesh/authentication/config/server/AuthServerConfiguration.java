package com.nivesh.authentication.config.server;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nivesh.authentication.entity.User;
import com.nivesh.authentication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Security configuration for nivesh.
 * Responsible for issuing token, managing keys and other service.
 *
 * @author Roshan
 */
@Configuration
public class AuthServerConfiguration {

    /**
     * URI of auth server
     */
    @Value("${nivesh.config.auth.url}")
    private String issuerUrl;

    /**
     * Private key location.
     */
    @Value("classpath:keys/private.pem")
    private Resource privateKeyFile;

    /**
     * Public key location.
     */
    @Value("classpath:keys/public.pem")
    private Resource publicKeyFile;

    /**
     * Repository for managing OAuth2 registered client.
     * Responsible for storing and retrieving client registration details.
     *
     * @param jdbcTemplate template used for executing SQL operations.
     * @return {@link JdbcRegisteredClientRepository}
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcRegisteredClientRepository(jdbcTemplate);
    }


    /**
     * Creates a JDBC-based service for managing authorizations.
     * Stores and retrieves authorization related data like tokens.
     *
     * @param jdbcTemplate for executing SQL operations.
     * @param registeredClientRepository Manages registered clients.
     * @return {@link JdbcOAuth2AuthorizationService}
     */
    @Bean
    public OAuth2AuthorizationService oAuth2AuthorizationService(JdbcTemplate jdbcTemplate,
                                                                 RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);
    }


    /**
     * Service for managing authorization consents.
     * Stores user consent information for OAuth2 clients, including roles and permissions.
     *
     * @param jdbcTemplate for executing SQL operations.
     * @param registeredClientRepository Manages registered clients.
     * @return {@link JdbcOAuth2AuthorizationConsentService}
     */
    @Bean
    public OAuth2AuthorizationConsentService oAuth2AuthorizationConsentService(JdbcTemplate jdbcTemplate,
                                                                               RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationConsentService(jdbcTemplate, registeredClientRepository);
    }


    /**
     * Configures authorization server settings.
     *
     * @return {@link AuthorizationServerSettings}
     */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().issuer(issuerUrl).build();
    }


    /**
     * Configures the filter chain for authorization server.
     * Enables filter for all auth endpoints.
     *
     * @param httpSecurity HTTP security configuration object.
     * @return Auth server {@link SecurityFilterChain}
     */
    @Bean
    @Order(1)
    public SecurityFilterChain serverSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
        OAuth2AuthorizationServerConfigurer serverConfigurer = new OAuth2AuthorizationServerConfigurer();

        httpSecurity
                .securityMatcher(serverConfigurer.getEndpointsMatcher())
                .authorizeHttpRequests(req -> req.anyRequest().authenticated())
                .csrf(AbstractHttpConfigurer::disable)
                .with(serverConfigurer, configurer ->
                        configurer
                                .authorizationEndpoint(Customizer.withDefaults())
                                .oidc(Customizer.withDefaults())
                                .tokenEndpoint(Customizer.withDefaults())
                                .tokenIntrospectionEndpoint(Customizer.withDefaults())
                                .tokenRevocationEndpoint(Customizer.withDefaults()))
                .exceptionHandling(exception -> exception
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)));

        return httpSecurity.build();
    }


    /**
     * Provides User details for authentication and authorization.
     *
     * @param userRepository repository responsible for managing users.
     * @return {@link org.springframework.security.core.userdetails.User} to avoid exceptions
     */
    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return email -> {
            User user = userRepository.findByEmailWithRolesAndPermissions(email).orElseThrow(
                    () -> new UsernameNotFoundException("User email not found.")
            );

            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .authorities(user.getAuthorities())
                    .build();
        };
    }


    /**
     * Using BcryptPasswordEncoder for encoding passwords.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    /**
     * Configures the RSAKey. Responsible for Signing and validating token.
     * Loads private key for signing token
     *
     * @param publicKey Used for validating token
     * @return {@link ImmutableJWKSet} key
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource(RSAPublicKey publicKey) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        RSAPrivateKey privateKey = loadPrivateKey();
        RSAKey key = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID("key-v1")
                .build();
        JWKSet jwkSet = new JWKSet(key);
        return new ImmutableJWKSet<>(jwkSet);
    }


    /**
     * Configures Jwt encoder for signing jwt tokens.
     *
     * @param jwkSource holds the keys
     * @return {@link NimbusJwtEncoder}
     */
    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource){
        return new NimbusJwtEncoder(jwkSource);
    }


    /**
     * Configures Public Key for validating token.
     */
    @Bean
    public RSAPublicKey publicKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String pem = new String(publicKeyFile.getInputStream().readAllBytes())
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(pem);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(spec);
    }


    /**
     * Load the private key from pem file
     */
    private RSAPrivateKey loadPrivateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String pem = new String(privateKeyFile.getInputStream().readAllBytes())
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(pem);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(spec);
    }
}
