package com.gkev.spring_redis.Mapper;

import com.gkev.spring_redis.DTO.FoodDTO;
import com.gkev.spring_redis.Entity.FoodEntity;
import com.gkev.spring_redis.Entity.FoodPriceEntity;
import org.springframework.stereotype.Component;

@Component
public class FoodDTOMapper {
    public FoodDTO tofoodDTO(FoodEntity foodEntity, FoodPriceEntity foodPriceEntity){
        return new FoodDTO(
                foodEntity.getLocalName(),
                foodEntity.getEnglishName(),
                foodPriceEntity.getPricePerKg()
        );
    }
}
