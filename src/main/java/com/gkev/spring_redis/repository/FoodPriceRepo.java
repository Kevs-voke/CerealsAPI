package com.gkev.spring_redis.repository;

import com.gkev.spring_redis.Entity.FoodPriceEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FoodPriceRepo extends ReactiveCrudRepository<FoodPriceEntity, Integer> {
    Mono<FoodPriceEntity> findByFoodId(Integer foodId);
    Flux<FoodPriceEntity> findBypricePerKgBetween(double min, double max);

}
