package com.gkev.spring_redis.Filters;

import com.gkev.spring_redis.service.JwtService;
import com.gkev.spring_redis.service.MyUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements WebFilter {

    private final JwtService jwtService;
    private final MyUserDetailsService userDetailsService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        HttpCookie authCookie =
                exchange.getRequest()
                        .getCookies()
                        .getFirst("auth_token");

        if (authCookie == null) {
            return chain.filter(exchange);
        }

        String token = authCookie.getValue();

        if (token == null || token.isBlank()) {
            return chain.filter(exchange);
        }

        String username;

        try {
            username = jwtService.extractUsername(token);
        } catch (Exception e) {
            return chain.filter(exchange);
        }


        return userDetailsService.findByUsername(username)
                .filter(userDetails ->  jwtService.validateToken(token, userDetails))
                .flatMap(userDetails -> {
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
                    SecurityContextImpl sc =new SecurityContextImpl(auth);
                    return chain.filter(exchange)
                            .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(sc)));

                })
                .switchIfEmpty(chain.filter(exchange));
    }
}
