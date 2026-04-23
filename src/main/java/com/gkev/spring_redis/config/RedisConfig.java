package com.gkev.spring_redis.config;

import com.gkev.spring_redis.DTO.FoodDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;


@Configuration
public class RedisConfig {


    @Bean
    public ReactiveRedisTemplate<String, FoodDTO> reactiveFoodRedisTemplate(
            ReactiveRedisConnectionFactory factory
    ) {
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        JacksonJsonRedisSerializer<FoodDTO> valueSerializer =
                new JacksonJsonRedisSerializer<>(FoodDTO.class);

        RedisSerializationContext<String, FoodDTO> context = RedisSerializationContext
                .<String, FoodDTO>newSerializationContext(keySerializer)
                .key(keySerializer)
                .value(valueSerializer)
                .hashKey(keySerializer)
                .hashValue(valueSerializer)
                .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }

}
