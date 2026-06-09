package com.gkev.spring_redis.repository.RepoImpl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gkev.spring_redis.DTO.OrderItemResponseDTO;
import com.gkev.spring_redis.DTO.OrderResponseDTO;
import com.gkev.spring_redis.repository.OrdersRepo;
import lombok.AllArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Repository
@AllArgsConstructor
public class OrdersRepoImp implements OrdersRepo {

    private final DatabaseClient client;
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Flux<OrderResponseDTO> viewOrders(Long userId) {

        return client.sql("""
                    SELECT
                          ord.order_id,
                          ord.customer_id,
                          ord.order_status,
                          ord.order_date,

                          COALESCE(
                              json_agg(
                                  json_build_object(
                                      'foodName', oi.food_name,
                                      'quantity', oi.quantity,
                                      'pricePerKg', oi.price_per_kg
                                  )
                              ) FILTER (WHERE oi.order_id IS NOT NULL),
                              '[]'::json
                          ) AS items,

                          COALESCE(
                              SUM(oi.quantity * oi.price_per_kg),
                              0
                          ) AS total_price

                      FROM orders ord
                      LEFT JOIN order_items oi
                          ON oi.order_id = ord.order_id

                      WHERE ord.customer_id = :usr_id

                      GROUP BY
                          ord.order_id,
                          ord.customer_id,
                          ord.order_status,
                          ord.order_date

                      ORDER BY ord.order_id;
                """)
                .bind("usr_id", userId)
                .map((row, meta) -> {

                    String itemsJson = row.get("items", String.class);

                    return new OrderResponseDTO(
                            row.get("order_id", Long.class),

                            parseItems(itemsJson),

                            safeDouble(row.get("total_price", BigDecimal.class)),

                            row.get("order_status", String.class),

                            Objects.requireNonNull(row.get("order_date", LocalDateTime.class)).toString()
                    );
                })
                .all();
    }

    private List<OrderItemResponseDTO> parseItems(String json) {
        try {
            return mapper.readValue(
                    json,
                    new TypeReference<>() {
                    }
            );
        } catch (Exception e) {
            return List.of();
        }
    }


    private double safeDouble(BigDecimal value) {
        return value != null ? value.doubleValue() : 0.0;
    }
}