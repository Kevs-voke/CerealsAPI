package com.gkev.spring_redis.service;

import com.gkev.spring_redis.Model.UserPrincipal;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import reactor.core.publisher.Mono;

public class MyUserDetailsService implements ReactiveUserDetailsService {
    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return getUserWithRoles(username)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found")))
                .map(u -> new UserPrincipal(u.user(), u.roles()));
    }

    private Mono<UserDetails> getUserWithRoles(String username) {
    }
}
