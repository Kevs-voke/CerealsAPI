package com.gkev.spring_redis.service;

import com.gkev.spring_redis.DTO.*;
import com.gkev.spring_redis.Entity.*;
import com.gkev.spring_redis.Exceptions.NoServiceException;
import com.gkev.spring_redis.Exceptions.ResourceNotFound;
import com.gkev.spring_redis.Mapper.FoodDTOMapper;
import com.gkev.spring_redis.repository.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FoodService {

    private final ReactiveRedisTemplate<String, FoodDTO> foodredisTemplate;
    private final FoodRepo foodRepo;
    private final FoodDTOMapper foodDTOMapper;
    private final FoodPriceRepo foodPriceRepo;
    private final UsersRepo usersRepo;
    private final OrderRepo orderRepository;
    private final OrderItemRepo orderItemRepo;
    private final OrdersRepo ordersRepo;
    private final String FOOD_RANGE_PRICE_ZSET = "food:price";
    private static final Logger log = LoggerFactory.getLogger(FoodService.class);


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
                .switchIfEmpty(fetchFoodByPriceDB(min, max));
    }

    private String foodKey(FoodDTO food) {
        return "food:" + food.english_name().toLowerCase();
    }

    private Mono<FoodDTO> fetchFoodByNameFromDb(String name) {
        return foodRepo.findByEnglishNameIgnoreCase(name)
                .flatMap(foodEntity ->
                        foodPriceRepo.findByFoodId(foodEntity.getFoodId())
                                .map(foodPrice -> foodDTOMapper.tofoodDTO(foodEntity, foodPrice))
                )
                .switchIfEmpty(Mono.error(new ResourceNotFound(
                        "Not found Food of that name", new Throwable())));
    }

    private Mono<FoodDTO> noService(Throwable t) {
        return Mono.error(new NoServiceException("Service Down! Please try again later", t));
    }

    private Flux<FoodDTO> fetchFoodByPriceDB(double min, double max) {
        return foodPriceRepo
                .findBypricePerKgBetween(min, max)
                .flatMap(foodprice ->
                        foodRepo.findByFoodId(foodprice.getFoodId())
                                .map(food -> foodDTOMapper.tofoodDTO(food, foodprice))
                )
                .switchIfEmpty(Mono.error(new ResourceNotFound(
                        "Not found food of that price", new Throwable())));
    }


    public Mono<OrderResponseDTO> processOrder(Long userId, OrderRequestDTO orderRequest) {
        return usersRepo.findById(userId.intValue())
                .switchIfEmpty(Mono.error(new ResourceNotFound(
                        "User not found with id: " + userId, new Throwable())))
                .flatMap(user -> Flux.fromIterable(orderRequest.items())
                        .flatMap(item ->
                                foodRepo.findByEnglishNameIgnoreCase(item.foodName())
                                        .switchIfEmpty(Mono.error(new ResourceNotFound(
                                                "Food not found: " + item.foodName(), new Throwable())))
                                        .flatMap(food -> foodPriceRepo.findByFoodId(food.getFoodId())
                                                .switchIfEmpty(Mono.error(new ResourceNotFound(
                                                        "Price not found for: " + item.foodName(), new Throwable())))
                                                .map(price -> createOrderItem(food, price, item.quantity()))
                                        )
                        )
                        .collectList()
                        .flatMap(items -> {
                            if (items.isEmpty()) {
                                return Mono.error(new RuntimeException("No valid items in order"));
                            }

                            double total = items.stream()
                                    .mapToDouble(item -> item.getPricePerKg() * item.getQuantity())
                                    .sum();

                            OrderEntity newOrder = new OrderEntity();
                            newOrder.setCustomerId(user.getUserId());
                            newOrder.setOrderTotal(total);
                            newOrder.setOrderStatus("pending");
                            newOrder.setOrderDate(LocalDateTime.now());

                            return orderRepository.save(newOrder)
                                    .flatMap(savedOrder -> saveOrderItems(savedOrder, items));
                        })
                );
    }

    private OrderItemsEntity createOrderItem(FoodEntity food, FoodPriceEntity price, double quantity) {
        OrderItemsEntity entity = new OrderItemsEntity();
        entity.setFoodId(food.getFoodId());
        entity.setFoodName(food.getEnglishName());
        entity.setQuantity(quantity);
        entity.setPricePerKg(price.getPricePerKg().doubleValue());
        return entity;
    }


    private Mono<OrderResponseDTO> saveOrderItems(OrderEntity savedOrder, List<OrderItemsEntity> items) {
        Integer orderId = savedOrder.getOrderId();

        return Flux.fromIterable(items)
                .map(item -> { item.setOrderId(orderId); return item; })
                .flatMap(orderItemRepo::save)
                .collectList()
                .map(savedItems -> buildResponse(savedOrder, savedItems));
    }

    private OrderResponseDTO buildResponse(OrderEntity savedOrder, List<OrderItemsEntity> savedItems) {
        List<OrderItemResponseDTO> dtoItems = savedItems.stream()
                .map(item -> new OrderItemResponseDTO(
                        item.getFoodName(),
                        item.getQuantity(),
                        item.getPricePerKg()
                ))
                .toList();

        return new OrderResponseDTO(
                (long) savedOrder.getOrderId(),
                dtoItems,
                savedOrder.getOrderTotal(),
                savedOrder.getOrderStatus(),
                savedOrder.getOrderDate().toString()
        );
    }

    public Flux<OrderResponseDTO> viewOrders(Long userId) {
        return ordersRepo.viewOrders(userId)
                .doOnNext(orders -> log.info("Fetched order: {}", orders))
                .doOnComplete(() -> log.info("Orders fetched"));

    }
}