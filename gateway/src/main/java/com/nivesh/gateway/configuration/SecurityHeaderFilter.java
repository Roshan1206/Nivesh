package com.nivesh.gateway.configuration;

import com.nivesh.library.constant.Constants;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class SecurityHeaderFilter implements GlobalFilter, Ordered {

    private static final List<String> RESTRICTED_HEADERS = List.of(Constants.INTERNAL_ROLE_HEADER_NAME, Constants.SOURCE_SERVICE_HEADER_NAME);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest sanitizeRequest = exchange.getRequest()
                .mutate()
                .headers(headers -> RESTRICTED_HEADERS.forEach(headers::remove))
                .build();
        return chain.filter(exchange.mutate().request(sanitizeRequest).build());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
