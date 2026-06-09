package com.gkev.spring_redis.Entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("order_items")
public class OrderItemsEntity {

    @Id
    private Integer id;

    @Column("order_id")
    private int orderId;

    @Column("food_id")
    private int foodId;

    @Column("food_name")
    private String foodName;

    private double quantity;

    @Column("price_per_kg")
    private double pricePerKg;
}