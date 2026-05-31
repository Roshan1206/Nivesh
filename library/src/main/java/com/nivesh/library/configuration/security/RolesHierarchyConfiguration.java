package com.nivesh.library.configuration.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;

/**
 * Configures the roles properly for user.
 */
@Configuration
public class RolesHierarchyConfiguration {

    /**
     * Define role hierarchy for user. {@code .role("A").implies("B")} mean A contains all permission of B.
     * It should be in higher to lower order
     */
    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role("SUPER_ADMIN").implies("ADMIN")
                .role("ADMIN").implies("BRANCH_MGR")
                .role("BRANCH_MGR").implies("TELLER")
                .role("CUSTOMER_ACTIVE").implies("CUSTOMER_REGISTERED")
                .role("CUSTOMER_REGISTERED").implies("CUSTOMER")
                .build();
    }


    /**
     * Inject hierarchy into expression handler {@code @PreAuthorize},
     * {@code hasAuthority()}. Without this, spring will ignore the hierarchy.
     */
    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy) {
        var handler = new DefaultMethodSecurityExpressionHandler();
        handler.setRoleHierarchy(roleHierarchy);
        return handler;
    }
}
