/*
 *    Copyright 2020 bithon.org
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.bithon.demo.user.service.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.config.Config;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * @author frank.chen021@outlook.com
 * @date 2021/3/21 20:15
 */
@Slf4j
@Component
public class RedisCache {

    private final ObjectMapper objectMapper;
    private final List<StringRedisTemplate> redisClients = new ArrayList<>();
    private final AtomicInteger index = new AtomicInteger(0);

    public RedisCache(ObjectMapper objectMapper, RedisProperties redisProperties) throws Exception {
        this.objectMapper = objectMapper;

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisProperties.getHost(), redisProperties.getPort());
        config.setDatabase(redisProperties.getDatabase());

        // Jedis
        {
            JedisConnectionFactory connectionFactory = new JedisConnectionFactory(config);
            connectionFactory.afterPropertiesSet();
            redisClients.add(new StringRedisTemplate(connectionFactory));
        }

        // Lettuce
        {
            LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(config);
            lettuceConnectionFactory.afterPropertiesSet();
            redisClients.add(new StringRedisTemplate(lettuceConnectionFactory));
        }

        // Redisson
        {
            Config redissConfig = new Config();
            redissConfig.useSingleServer()
                        .setAddress(String.format("redis://%s:%d", redisProperties.getHost(), redisProperties.getPort()))
                        .setDatabase(redisProperties.getDatabase());
            RedissonConnectionFactory redissonConnectionFactory = new RedissonConnectionFactory(redissConfig);
            redissonConnectionFactory.afterPropertiesSet();
            redisClients.add(new StringRedisTemplate(redissonConnectionFactory));
        }
    }

    private StringRedisTemplate getRedisClient() {
        int idx = index.getAndIncrement();
        return redisClients.get(idx % redisClients.size());
    }

    public <T> T get(String key, Duration expiration, Class<T> clazz, Supplier<T> supplier) {
        StringRedisTemplate redis = getRedisClient();
        String v = redis.opsForValue().get(key);
        if (v != null) {
            try {
                return objectMapper.readValue(v, clazz);
            } catch (JsonProcessingException e) {
                redis.delete(key);
            }
        }

        T obj = supplier.get();
        if (obj != null) {
            try {
                v = objectMapper.writeValueAsString(obj);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            redis.opsForValue().setIfAbsent(key, v, expiration.getSeconds(), TimeUnit.SECONDS);
        }
        return obj;
    }

    public void remove(List<String> uids) {
        StringRedisTemplate redis = getRedisClient();
        redis.delete(uids);
    }

    public void increase(String key) {
        StringRedisTemplate redis = getRedisClient();
        redis.opsForValue().increment(key);
    }
}
