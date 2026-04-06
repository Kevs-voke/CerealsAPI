package com.gkev.spring_redis.Mapper;

import com.gkev.spring_redis.DTO.RegistrationResponseDTO;

public class RegistrationResponseDTOMapper {
    public static RegistrationResponseDTO toResponse(String message, String username){
        return new RegistrationResponseDTO(message,username);
    }
}
