package com.gkev.spring_redis.controller;

import com.gkev.spring_redis.DTO.FoodDTO;
import com.gkev.spring_redis.DTO.OrderRequestDTO;
import com.gkev.spring_redis.DTO.OrderResponseDTO;
import com.gkev.spring_redis.Model.UserPrincipal;
import com.gkev.spring_redis.service.FoodService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CustomerController {

    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);
    private final FoodService foodService;

    public CustomerController(FoodService foodService) {
        this.foodService = foodService;
    }

    @GetMapping("/search/{name}")
    public Mono<ResponseEntity<FoodDTO>> getFoodByName(@PathVariable("name") String name) {
        log.info("Searching for food by name: {}", name);
        return foodService.getFoodByName(name)
                .map(food -> {
                    log.info("Food found: {}", food);
                    return ResponseEntity.ok(food);
                });
    }

    @GetMapping("/search/price")
    public Flux<FoodDTO> getFoodByPriceRange(@RequestParam double min, @RequestParam double max) {
        log.info("Searching for food in price range: {} - {}", min, max);
        return foodService.getFoodByPriceRange(min, max)
                .doOnNext(food -> log.info("Food found in price range: {} - {}", min, max));
    }

    @PostMapping("/order/make-order")
    public Mono<ResponseEntity<OrderResponseDTO>> setOrder(
            Authentication authentication,
            @RequestBody @Valid OrderRequestDTO orderRequest) {

        log.info("Placing order. Items count: {}",
                orderRequest.items() != null ? orderRequest.items().size() : 0);


        if (authentication == null || !authentication.isAuthenticated()) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        Long userId = principal.getUserId();

        return foodService.processOrder(userId, orderRequest)
                .map(savedOrder -> {
                    log.info("Order placed successfully. Order ID: {}", savedOrder.id());
                    return ResponseEntity.status(HttpStatus.CREATED).body(savedOrder);
                });
    }
    @GetMapping("/order/get-orders")
    public Mono<List<OrderResponseDTO>> getMyOrders(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        Long userId = principal.getUserId();
        log.info("User: {} attempts to view orders", userId);
        return foodService.viewOrders(userId)
                .doOnNext(order -> log.info("Emitting order: {}", order))
                .collectList();
    }
}