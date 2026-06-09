package com.gkev.spring_redis.DTO;

public record OrderItemResponseDTO(
        String foodName,
        double quantity,
        double pricePerKg
) {}
