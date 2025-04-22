package com.devteria.Demo_Spring_boot.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate (RedisConnectionFactory connectionFactory){//connectionFactory cổng kết nối tới Redis
        //Kết nối tới Redis có định nghĩa trong file yaml
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        //Key và value serializer(tuần tự hóa) - tùy ý
        //Redis lưu trữ dưới dạng Byte nên cần Serializer chuyển Java -> byte và ngược lại
        template.setKeySerializer(new StringRedisSerializer());//String -> byte
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer()); //byte -> String

        return template;
    }
}
