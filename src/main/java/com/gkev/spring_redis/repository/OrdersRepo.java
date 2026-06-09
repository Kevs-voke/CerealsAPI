package com.gkev.spring_redis.repository;

import com.gkev.spring_redis.DTO.OrderResponseDTO;
import reactor.core.publisher.Flux;

public interface OrdersRepo {
    Flux<OrderResponseDTO> viewOrders(Long userId);
}
