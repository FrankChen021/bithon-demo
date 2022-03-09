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

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author frank.chen021@outlook.com
 * @date 2021/3/21 20:15
 */
@Slf4j
@Component
public class RedisComponent {

    @Autowired
    StringRedisTemplate redis;


    @Scheduled(fixedDelay = 1000)
    void redisKVOperation() {
        log.info("Redis set/get...");
        redis.opsForValue().set("user.name", "frank.chen");
        redis.opsForValue().get("user.name");
    }

    @Scheduled(fixedDelay = 3000)
    void redisHashOperation() {
        log.info("Redis hash operation...");
        redis.opsForHash().put("user.info", "age", "18");
        redis.opsForHash().put("user.info", "gender", "male");
        redis.opsForHash().put("user.info", "addr", "Chengdu");
        redis.opsForHash().keys("user.info");

        Runnable r = new Runnable(){
            @Override
            public void run() {

            }
        };
        r.run();

    }
}
