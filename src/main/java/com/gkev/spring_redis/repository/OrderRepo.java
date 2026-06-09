package com.gkev.spring_redis.repository;

import com.gkev.spring_redis.Entity.OrderEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface OrderRepo extends ReactiveCrudRepository<OrderEntity, Integer> {}