package com.gkev.spring_redis.controller;


import com.gkev.spring_redis.DTO.FoodDTO;
import com.gkev.spring_redis.service.FoodService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class CustomerController {
    private final FoodService foodService;

    public CustomerController(FoodService foodService) {
        this.foodService = foodService;
    }


    @GetMapping("search/{name}")
    public Mono<ResponseEntity<FoodDTO>> getFoodByName(@PathVariable("name") String name) {
        return foodService
                .getFoodByName(name)
                .map(response ->
                        ResponseEntity
                        .status(HttpStatus.OK)
                        .body(response))
                .switchIfEmpty(
                        Mono.just(ResponseEntity.notFound().build())
                );
    }

    @GetMapping("/search/price")
    public Mono<ResponseEntity<Flux<FoodDTO>>> getFoodByPriceRange(
            @RequestParam double min,
            @RequestParam double max
    ){
        Flux<FoodDTO> food = foodService.getFoodByPriceRange(min, max);

        return food.hasElements()
                .flatMap(hasElements -> {
                    if (hasElements) {
                        return Mono.just(ResponseEntity.ok(food));
                    } else {
                        return Mono.just(ResponseEntity.notFound().build());
                    }
                });
    }
//    @PostMapping("/order/make-order")
//    public Mono<ResponseEntity<OrderDTO>> setOrder(){
//        re
//    }

}