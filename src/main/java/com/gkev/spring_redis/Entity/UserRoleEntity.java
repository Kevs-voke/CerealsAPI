package com.gkev.spring_redis.Entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("user_roles")
public class UserRoleEntity {

    @Id
    private int id;

    @Column("user_id")
    private int userId;

    @Column("role_id")
    private int roleId;
}
