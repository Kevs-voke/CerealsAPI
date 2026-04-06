package com.gkev.spring_redis.repository;

import com.gkev.spring_redis.Entity.UserRoleEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRoleRepo extends ReactiveCrudRepository<UserRoleEntity,Integer> {

    @Override
    Flux<UserRoleEntity> findByUserId(Integer userId);
}
