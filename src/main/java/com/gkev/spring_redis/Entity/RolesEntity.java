package com.gkev.spring_redis.Entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("roles")
public class RolesEntity {
    @Id
    private int id;
    private String name;
}
