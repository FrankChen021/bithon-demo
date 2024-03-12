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

package org.bithon.demo.user.service.event.kafka;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.bithon.demo.user.service.redis.RedisCache;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.SmartLifecycle;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.BatchMessageListener;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author frank.chen021@outlook.com
 * @date 2023/6/19 21:04
 */
@Slf4j
@Service
public class KafkaEventConsumer implements BatchMessageListener<String, byte[]>, SmartLifecycle {
    private final KafkaProperties kafkaProperties;
    protected final ObjectMapper objectMapper;
    private ConcurrentMessageListenerContainer<String, String> consumerContainer;
    private final RedisCache redis;
    private final ApplicationContext applicationContext;

    public KafkaEventConsumer(KafkaProperties kafkaProperties, RedisCache redis, ApplicationContext applicationContext) {
        this.kafkaProperties = kafkaProperties;
        this.redis = redis;
        this.applicationContext = applicationContext;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void start() {
        Map<String, Object> consumerProperties = kafkaProperties.buildConsumerProperties();
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());

        ContainerProperties containerProperties = new ContainerProperties(kafkaProperties.getProperties().get("topic"));
        containerProperties.setAckMode(ContainerProperties.AckMode.TIME);
        containerProperties.setAckTime(kafkaProperties.getListener().getAckTime().toMillis());
        containerProperties.setPollTimeout(kafkaProperties.getListener().getPollTimeout().toMillis());
        containerProperties.setGroupId(kafkaProperties.getConsumer().getGroupId());
        containerProperties.setClientId(kafkaProperties.getClientId());

        DefaultKafkaConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProperties);
        consumerFactory.addListener(new ConsumerFactory.Listener<>() {
            @Override
            public void consumerAdded(String id, Consumer<String, String> consumer) {
            }

            @Override
            public void consumerRemoved(String id, Consumer<String, String> consumer) {
            }
        });
        consumerContainer = new ConcurrentMessageListenerContainer<>(consumerFactory, containerProperties);

        // the Spring Kafka uses the bean name as prefix of thread name
        // Since tracing records thread name automatically to span logs, we explicitly set the bean name to improve the readability of span logs
        consumerContainer.setBeanName(this.getClass().getSimpleName());

        consumerContainer.setupMessageListener(this);
        consumerContainer.setConcurrency(kafkaProperties.getListener().getConcurrency());
        consumerContainer.setApplicationEventPublisher(applicationContext);
        consumerContainer.setApplicationContext(applicationContext);
        consumerContainer.start();

        log.info("Starting Kafka consumer...");
    }

    @Override
    public void stop() {
        log.info("Stopping Kafka consumer...");
        if (consumerContainer != null) {
            consumerContainer.stop(true);
        }
    }

    @Override
    public boolean isRunning() {
        return consumerContainer != null && consumerContainer.isRunning();
    }

    @Override
    public void onMessage(List<ConsumerRecord<String, byte[]>> records) {
        records.forEach((record) -> redis.incr(new String(record.value())));
    }
}
