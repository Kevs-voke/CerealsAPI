package com.gkev.spring_redis.service;

import com.gkev.spring_redis.DTO.FoodDTO;
import com.gkev.spring_redis.Exceptions.NoServiceException;
import com.gkev.spring_redis.Exceptions.ResourceNotFound;
import com.gkev.spring_redis.Mapper.FoodDTOMapper;
import com.gkev.spring_redis.repository.FoodPriceRepo;
import com.gkev.spring_redis.repository.FoodRepo;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class FoodService {
    private final ReactiveRedisTemplate<String, FoodDTO> foodredisTemplate;
    private final FoodRepo foodRepo;
    private final FoodDTOMapper foodDTOMapper;
    private final FoodPriceRepo foodPriceRepo;
    private final String FOOD_RANGE_PRICE_ZSET = "food:price";



    @Retry(name = "database")
    @CircuitBreaker(name = "database", fallbackMethod = "noService")
    public Mono<FoodDTO> getFoodByName(String name) {
        String key = "food:" + name;
        Duration slidingTTL = Duration.ofMinutes(30);

        return foodredisTemplate.opsForValue()
                .get(key)
                .flatMap(food ->
                        foodredisTemplate.expire(key, slidingTTL)
                                .thenReturn(food)
                )
                .switchIfEmpty(
                        fetchFoodByNameFromDb(name)
                                .flatMap(food ->
                                        foodredisTemplate.opsForValue()
                                                .set(key, food, slidingTTL)
                                                .thenReturn(food)
                                )
                );
    }
    @Retry(name = "database")
    @CircuitBreaker(name = "database", fallbackMethod = "noService")
    public Flux<FoodDTO> getFoodByPriceRange(double min, double max) {
        Duration slidingTTL = Duration.ofMinutes(30);
        Range<Double> priceRange = Range.closed(min, max);

        return foodredisTemplate.opsForZSet()
                .rangeByScore(FOOD_RANGE_PRICE_ZSET, priceRange)
                .flatMap(food ->
                        foodredisTemplate.expire(foodKey(food), slidingTTL)
                                .onErrorResume(e -> Mono.just(false))
                                .thenReturn(food)
                )
                .switchIfEmpty(
                    fetchFoodByPriceDB(min, max)
                );
    }

    private String foodKey(FoodDTO food) {
        return "food:" + food.english_name().toLowerCase();
    }

    private Mono<FoodDTO> fetchFoodByNameFromDb(String name) {
        return foodRepo.findByEnglishName(name)
                .flatMap(foodEntity ->
                        foodPriceRepo.findByFoodId(foodEntity.getFoodId())
                                .map(foodPrice ->
                                        foodDTOMapper.tofoodDTO(foodEntity, foodPrice
                                        )
                                )
                )
                .switchIfEmpty(Mono.error(new ResourceNotFound("Not found Food of that name", new Throwable())));
    }


    private Mono<FoodDTO> noService(Throwable t) {
        return Mono.error(new NoServiceException("Service Down! Please try again later", t));
    }

    private Flux<FoodDTO> fetchFoodByPriceDB(double min, double max) {
        return foodPriceRepo
                .findBypricePerKgBetween(min, max)
                .flatMap(foodprice ->
                        foodRepo.findByFoodId(foodprice.getFoodId())
                                .map(
                                        food ->
                                                foodDTOMapper.tofoodDTO(food,foodprice)
                                )
                )
                .switchIfEmpty((Mono.error(new ResourceNotFound("Not found food of that price", new Throwable()))));

    }
}