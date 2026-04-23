package com.gkev.spring_redis.Mapper;

import com.gkev.spring_redis.DTO.RegistrationResponseDTO;
import com.gkev.spring_redis.Entity.UsersEntity;
import org.springframework.stereotype.Component;

@Component
public class RegistrationResponseDTOMapper {
    public  RegistrationResponseDTO toResponse(UsersEntity user, String authToken ){
        return new RegistrationResponseDTO(user.getEmail(), authToken);
    }
}
