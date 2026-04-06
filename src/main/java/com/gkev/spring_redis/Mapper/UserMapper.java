package com.gkev.spring_redis.Mapper;


import com.gkev.spring_redis.DTO.UserDTO;
import com.gkev.spring_redis.Entity.UsersEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UsersEntity toUsersEntity(UserDTO dto) {
        UsersEntity user = new UsersEntity();
        user.setUsername(dto.username());
        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());
        user.setEmail(dto.email());
        user.setPhoneNumber(dto.phoneNumber());
        return user;
    }
}