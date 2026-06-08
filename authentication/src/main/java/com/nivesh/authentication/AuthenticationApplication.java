package com.nivesh.authentication;

import com.nivesh.authentication.config.properties.AuthCacheProperties;
import com.nivesh.library.annotation.EnableNiveshCache;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableConfigurationProperties(AuthCacheProperties.class)
@EnableJpaAuditing(auditorAwareRef = "auditConfig")
@EnableNiveshCache
@SpringBootApplication
public class AuthenticationApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthenticationApplication.class, args);
	}

}
