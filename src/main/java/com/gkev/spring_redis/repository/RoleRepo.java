package com.gkev.spring_redis.repository;

import com.gkev.spring_redis.Entity.RolesEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RoleRepo extends ReactiveCrudRepository<RolesEntity, Integer> {

    @Override
    Mono<RolesEntity> findById(Integer id);

    Flux<RolesEntity> findByName(String upperCase);
}
