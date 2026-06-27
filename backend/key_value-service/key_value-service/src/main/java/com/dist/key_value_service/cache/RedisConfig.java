package com.dist.key_value_service.cache;

import com.dist.key_value_service.dto.KVResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

@Configuration
public class RedisConfig {
        @Bean
        public RedisTemplate<String, KVResponse> redisTemplate(
                RedisConnectionFactory connectionFactory) {

            RedisTemplate<String, KVResponse> template = new RedisTemplate<>();
            template.setConnectionFactory(connectionFactory);

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            Jackson2JsonRedisSerializer<KVResponse> serializer =
                    new Jackson2JsonRedisSerializer<>(mapper, KVResponse.class);

            template.setKeySerializer(new StringRedisSerializer());
            template.setValueSerializer(serializer);
            template.setHashKeySerializer(new StringRedisSerializer());
            template.setHashValueSerializer(serializer);

            template.afterPropertiesSet();
            return template;
        }

//    @Bean
//    public RedisTemplate<String, KVResponse> redisTemplate(
//            RedisConnectionFactory connectionFactory) {
//
//        RedisTemplate<String, KVResponse> template = new RedisTemplate<>();
//
//        template.setConnectionFactory(connectionFactory);
//
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.registerModule(new JavaTimeModule());
//
//        GenericJackson2JsonRedisSerializer serializer =
//                new GenericJackson2JsonRedisSerializer(mapper);
//
//        template.setKeySerializer(new StringRedisSerializer());
//        template.setValueSerializer(serializer);
//
//        template.afterPropertiesSet();
//
//        return template;
//    }
}