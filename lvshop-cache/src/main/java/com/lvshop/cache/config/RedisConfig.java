package com.lvshop.cache.config;

import com.lvshop.common.pojo.ProductInfo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

/**
 * @author Galliano
 */
@Configuration
public class RedisConfig {

    // 改了默认的序列化器，让 对象 ==> json串
    @Bean
    public RedisTemplate<Object, ProductInfo> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, ProductInfo> template = new RedisTemplate<>();

        template.setConnectionFactory(redisConnectionFactory);

        Jackson2JsonRedisSerializer<ProductInfo> ser = new Jackson2JsonRedisSerializer<>(ProductInfo.class);
        template.setDefaultSerializer(ser);  //  可以将 对象 转成 json串

        return template;
    }
}
