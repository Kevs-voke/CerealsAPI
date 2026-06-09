package com.gkev.spring_redis.config;

import com.gkev.spring_redis.DTO.FoodDTO;
import io.github.bucket4j.distributed.proxy.AsyncProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.url}")
    private String redisUrl;

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

    @Bean
    public AsyncProxyManager<String> bucket4jAsyncProxyManager() {
        RedisClient redisClient = RedisClient.create(redisUrl);
        redisClient.setDefaultTimeout(Duration.ofSeconds(10));

        StatefulRedisConnection<String, byte[]> connection =
                redisClient.connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE));

        return LettuceBasedProxyManager.builderFor(connection)
                .build()
                .asAsync();
    }
}