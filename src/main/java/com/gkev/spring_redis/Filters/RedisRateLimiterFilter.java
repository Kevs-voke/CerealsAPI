package com.gkev.spring_redis.Filters;

import com.gkev.spring_redis.service.RateLimitingService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import org.springframework.http.server.reactive.ServerHttpRequest;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class RedisRateLimiterFilter implements WebFilter {

    private final RateLimitingService rateLimitingService;

    @NonNull
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String clientIp = getClientIp(exchange.getRequest());

        return Mono.fromFuture(
                        rateLimitingService.resolveAsyncBucket(clientIp)
                                .tryConsumeAndReturnRemaining(1)
                )
                .flatMap(probe -> {

                    if (probe.isConsumed()) {

                        // ✅ Correct way: Use beforeCommit for successful requests
                        exchange.getResponse().beforeCommit(() -> {
                            exchange.getResponse().getHeaders()
                                    .add("X-Rate-Limit-Remaining",
                                            String.valueOf(probe.getRemainingTokens()));
                            // Add more headers if needed:
                            // .add("X-Rate-Limit-Limit", "100");
                            // .add("X-Rate-Limit-Reset", "...");
                            return Mono.empty();
                        });

                        return chain.filter(exchange);
                    }

                    // === 429 Too Many Requests ===
                    long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000L;

                    var response = exchange.getResponse();
                    response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    response.getHeaders().add("X-Rate-Limit-Retry-After-Seconds",
                            String.valueOf(waitForRefill));

                    String jsonResponse = """
                            {
                              "status": %d,
                              "error": "Too Many Requests",
                              "message": "You have exhausted your API Request Quota",
                              "retryAfterSeconds": %d
                            }
                            """.formatted(HttpStatus.TOO_MANY_REQUESTS.value(), waitForRefill);

                    byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
                    var buffer = response.bufferFactory().wrap(bytes);

                    return response.writeWith(Mono.just(buffer));
                });
    }

    public static String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        if (request.getRemoteAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }
        return "unknown";
    }
}