package com.gkev.spring_redis.Filters;

import com.gkev.spring_redis.service.RateLimitingService;
import io.github.bucket4j.Bucket;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import org.springframework.http.server.reactive.ServerHttpRequest;

@Component
@RequiredArgsConstructor
public class RedisRateLimiterFilter implements WebFilter {

    private final RateLimitingService rateLimitingService;

    @NonNull
    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                              WebFilterChain chain) {

        String clientIp = getClientIp(exchange.getRequest());

        Bucket tokenBucket = rateLimitingService.resolveBucket(clientIp);

        return Mono.fromCallable(() ->
                        tokenBucket.tryConsumeAndReturnRemaining(1))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(probe -> {

                    if (probe.isConsumed()) {

                        exchange.getResponse()
                                .getHeaders()
                                .add("X-Rate-Limit-Remaining",
                                        String.valueOf(probe.getRemainingTokens()));

                        return chain.filter(exchange);
                    }

                    long waitForRefill =
                            probe.getNanosToWaitForRefill() / 1_000_000_000;

                    exchange.getResponse()
                            .setStatusCode(HttpStatus.TOO_MANY_REQUESTS);

                    exchange.getResponse()
                            .getHeaders()
                            .add("X-Rate-Limit-Retry-After-Seconds",
                                    String.valueOf(waitForRefill));

                    exchange.getResponse()
                            .getHeaders()
                            .setContentType(MediaType.APPLICATION_JSON);

                    String jsonResponse = """
                            {
                             "status": %s,
                             "error": "Too many Requests",
                             "message": "You have exhausted your API Request Quota",
                             "retryAfterSeconds": %s
                            }
                            """.formatted(
                            HttpStatus.TOO_MANY_REQUESTS.value(),
                            waitForRefill
                    );

                    byte[] bytes =
                            jsonResponse.getBytes(java.nio.charset.StandardCharsets.UTF_8);

                    var buffer =
                            exchange.getResponse()
                                    .bufferFactory()
                                    .wrap(bytes);

                    return exchange.getResponse()
                            .writeWith(Mono.just(buffer));
                });
    }

    public static String getClientIp(ServerHttpRequest request) {

        String xForwardedFor =
                request.getHeaders().getFirst("X-Forwarded-For");

        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp =
                request.getHeaders().getFirst("X-Real-IP");

        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        if (request.getRemoteAddress() != null) {
            return request.getRemoteAddress()
                    .getAddress()
                    .getHostAddress();
        }

        return "unknown";
    }
}