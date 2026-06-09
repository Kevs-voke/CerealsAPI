package com.gkev.spring_redis.repository;

import com.gkev.spring_redis.Entity.UsersEntity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UsersRepo extends ReactiveCrudRepository<UsersEntity, Integer>{

    Mono<UsersEntity> findByUsername(String username);
    Mono<Boolean> existsByEmail(String email);
    Mono<Boolean> existsByUsername(String username);
    Mono<Boolean> existsByPhoneNumber(String phoneNumber);

    Mono<UsersEntity> findByEmail(@Email(message = "Email should be valid") @NotBlank(message = "Email is required") String email);


}
