package com.gkev.spring_redis.Entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

    @Data
    @Table("food_price")
    public class FoodPriceEntity{
        @Id
        @Column("price_id")
        private Integer priceId;

        @Column("food_id")
        private Integer foodId;

        @Column("price_per_kg")
        private BigDecimal pricePerKg;

        @Column("price_date")
        private LocalDate priceDate;

    }
