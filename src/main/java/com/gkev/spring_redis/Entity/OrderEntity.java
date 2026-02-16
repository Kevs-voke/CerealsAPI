package com.gkev.spring_redis.Entity;

import lombok.Data;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Date;

@Data
@Table("orders")
public class OrderEntity {

    @Column("order_id")
    private int orderId;

    @Column("customer_id")
    private int customerId;

    @Column("order_total")
    private double orderTotal;

    @Column("order_status")
    private String orderStatus;

    @Column("order_date")
    private Date orderDate;

}
