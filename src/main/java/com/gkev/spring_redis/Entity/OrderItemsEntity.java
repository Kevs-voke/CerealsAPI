package com.gkev.spring_redis.Entity;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("order_items")
public class OrderItemsEntity {
    @Id
    @Column("order_id")
    private int orderID;

    @Column("customer_id")
    private int customerID;

    private double quantity;

    @Column("price_per_kg")
    private double pricePerKg;
    

}
