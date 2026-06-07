package com.nivesh.gateway.configuration.security;

//import com.nivesh.library.constant.Constants;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Web filter that applies security header behavior to gateway requests and responses.
 */
@Component
public class SecurityHeaderFilter implements GlobalFilter, Ordered {

    private static final List<String> RESTRICTED_HEADERS = List.of("X-Internal-Role", "X-Source-Service");


    /**
     * Adds security headers to the outgoing request.
     *
     * @param exchange The current ServerWebExchange.
     * @param chain The GatewayFilterChain to continue processing the request.
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest sanitizeRequest = exchange.getRequest()
                .mutate()
                .headers(headers -> RESTRICTED_HEADERS.forEach(headers::remove))
                .build();
        return chain.filter(exchange.mutate().request(sanitizeRequest).build());
    }


    /**
     * Returns the order in which this filter should be applied.
     *
     * @return The order of this filter.
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
