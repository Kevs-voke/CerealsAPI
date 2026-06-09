package com.gkev.spring_redis.repository;

import com.gkev.spring_redis.Entity.FoodEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface FoodRepo extends ReactiveCrudRepository<FoodEntity, Integer> {
    Mono<FoodEntity> findByEnglishNameIgnoreCase(String englishName);
    Mono<FoodEntity> findByFoodId(Integer foodId);
}
