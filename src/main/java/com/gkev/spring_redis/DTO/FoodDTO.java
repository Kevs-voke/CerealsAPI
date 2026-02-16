package com.gkev.spring_redis.DTO;

import java.math.BigDecimal;

public record FoodDTO(
    String local_name,
    String english_name,
    BigDecimal price
    )
{}
