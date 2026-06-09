package com.gkev.spring_redis.DTO;

import java.util.List;

public record OrderResponseDTO(
        Long id,
        List<OrderItemResponseDTO> items,
        Double totalPrice,
        String status,
        String createdAt
) { }