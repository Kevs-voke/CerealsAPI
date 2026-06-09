package com.gkev.spring_redis.repository;

import com.gkev.spring_redis.Entity.OrderItemsEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

// FIX Bug 6: generic ID type changed from Long to Integer to match entity field
public interface OrderItemRepo extends ReactiveCrudRepository<OrderItemsEntity, Integer> {}