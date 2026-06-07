package com.nivesh.library.configuration.cache;

import com.nivesh.library.configuration.EmailOtpSender;
import com.nivesh.library.service.OtpCacheService;
import com.nivesh.library.service.OtpSender;
import com.nivesh.library.service.JwtTokenService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * Configuration class enabled by {@link EnableOtpCache}.
 * Configures beans required for sending and validating OTPs.
 */
@AutoConfiguration(after = NiveshCacheAutoConfiguration.class)
@ConditionalOnProperty(name = "host", prefix = "spring.mail")
public class OtpCacheConfiguration {

//    @Bean
//    @ConditionalOnMissingBean(JavaMailSender.class)
//    public JavaMailSender mailSender(MailProperties properties) {
//        JavaMailSenderImpl sender = new JavaMailSenderImpl();
//        sender.setHost(properties.getHost());
//        sender.setPort(properties.getPort());
//        sender.setUsername(properties.getUsername());
//        sender.setPassword(properties.getPassword());
//
//        Properties props = sender.getJavaMailProperties();
//        props.put("mail.transport.put", "smtp");
//        props.put("mail.smtp.auth", "true");
//        props.put("mail.smtp.starttls.enable", "true");
//        return sender;
//    }

    /**
     * Provides an email-based OTP sender implementation.
     * Sends OTPs asynchronously with built-in retry mechanism.
     * Only created if not provided by the consuming application.
     *
     * @param mailSender Spring mail sender configured for SMTP
     * @return OtpSender implementation
     */
    @Bean
    @ConditionalOnMissingBean(EmailOtpSender.class)
    public OtpSender emailOtpSender(JavaMailSender mailSender) {
        return new EmailOtpSender(mailSender);
    }

    /**
     * Instantiates the OTP cache service for generating and validating OTPs.
     * Conditionally registers only if not provided by a consuming application.
     *
     * @param cacheManager the OTP cache manager
     * @param jwtTokenService for extracting email
     * @return OtpCacheService instance
     */
    @Bean
    @ConditionalOnMissingBean(OtpCacheService.class)
    public OtpCacheService otpCacheService(CacheManager cacheManager, JwtTokenService jwtTokenService,
                                           OtpSender sender) {
        return new OtpCacheService(cacheManager, jwtTokenService, sender);
    }
}
