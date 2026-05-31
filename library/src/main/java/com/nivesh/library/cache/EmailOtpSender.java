package com.nivesh.library.cache;

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
     * Creates a Async request using virtual thread to send OTP mail.
     * If failed, it will retry for 3 more attempts within delay for 2 sec
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
     * Recover method for Email failure
     */
    @Recover
    public void onEmailFailure(MailException exception, String email) {
        log.error("Failed to send OTP email to {} after all retires. Cause: {}", email, exception.getMessage());
    }
}
