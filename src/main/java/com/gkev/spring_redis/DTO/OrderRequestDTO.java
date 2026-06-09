package com.gkev.spring_redis.DTO;

import java.util.List;

public record OrderRequestDTO(
        List<OrderItemRequestDTO> items
) {
}
