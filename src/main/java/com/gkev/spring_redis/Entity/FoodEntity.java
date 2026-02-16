package com.gkev.spring_redis.Entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Data
@Table("foods")
public class FoodEntity {

    @Id
    @Column("food_id")   // maps DB column to Java field
    private int foodId;

    @Column("group_id")
    private int groupId;

    @Column("english_name")
    private String englishName;

    @Column("local_name")
    private String localName;
}
