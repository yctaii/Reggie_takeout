package com.yc.reggie.config;

import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig extends CachingConfigurerSupport{
    

    @Bean
    public RedisTemplate<Object,Object> redisTemplate(RedisConnectionFactory connectionFactory){
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        //默认的Key序列化器是：JDKSerializationRedisSerializer
        //导致redis key-value存储进去 key会多一堆前缀，从而后续无法取值
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        //而value的序列化器虽然也是JDKSerializationRedisSerializer
        //但是在后面取值时，会有反序列化的过程，所以不会有问题
        redisTemplate.setConnectionFactory(connectionFactory);
        return redisTemplate;
    }
}
