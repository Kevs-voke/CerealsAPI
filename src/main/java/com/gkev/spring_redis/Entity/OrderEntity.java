package com.gkev.spring_redis.Entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Table("orders")
public class OrderEntity {

    @Id
    @Column("order_id")
    private int orderId;

    @Column("customer_id")
    private int customerId;

    @Column("order_total")
    private double orderTotal;

    @Column("order_status")
    private String orderStatus;

    @Column("order_date")
    private LocalDateTime orderDate;

}
