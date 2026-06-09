package com.gkev.spring_redis.repository;

import com.gkev.spring_redis.Entity.OrderItemsEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface OrderItemRepo extends ReactiveCrudRepository<OrderItemsEntity, Integer> {}