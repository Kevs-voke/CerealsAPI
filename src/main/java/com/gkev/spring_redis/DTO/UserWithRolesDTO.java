package com.gkev.spring_redis.DTO;

import com.gkev.spring_redis.Entity.RolesEntity;
import com.gkev.spring_redis.Entity.UserRoleEntity;
import com.gkev.spring_redis.Entity.UsersEntity;

import java.util.List;

public record UserWithRolesDTO(UsersEntity user, List<RolesEntity> roles) {
}
