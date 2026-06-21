package com.nivesh.library.configuration;

import com.nivesh.library.service.OtpSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;

/**
 * Implementation of {@link OtpSender} to send OTP via email
 */
@Slf4j
//@Component
public class EmailOtpSender implements OtpSender {

    /**
     * Sends mail to user
     */
    private final JavaMailSender mailSender;

    /**
     * Injecting dependency using CI
     */
    public EmailOtpSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Sends OTP via email asynchronously with automatic retry on failure.
     * Uses virtual threads for efficient async execution.
     * Retries up to 3 times with exponential backoff on mail errors.
     *
     * @param email recipient email address
     * @param otp the OTP code to send
     */
    @Async("emailTaskExecutor")
    @Retryable(
            retryFor = { MailException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    @Override
    public void send(String email, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your OTP - Nivesh Bank");
        message.setText("Your OTP is: " + otp +". \nValid for 5 min. Do not share.");
        mailSender.send(message);
        log.debug("OTP sent to {}", email);
    }

    /**
     * Handles OTP email delivery failure after all retry attempts.
     * Logs the final error for monitoring and troubleshooting.
     *
     * @param exception the mail exception that caused the failure
     * @param email the email address where sending failed
     */
    @Recover
    public void onEmailFailure(MailException exception, String email) {
        log.error("Failed to send OTP email to {} after all retires. Cause: {}", email, exception.getMessage());
    }
}
