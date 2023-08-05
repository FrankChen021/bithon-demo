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

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.bithon.demo.user.service.event.IEventPublisher;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * @author frank.chen021@outlook.com
 * @date 2023/6/19 21:04
 */
@Service
public class KafkaEventPublisher implements AutoCloseable, IEventPublisher {

    private final KafkaTemplate<String, String> producer;
    private final String topic;

    public KafkaEventPublisher(KafkaProperties kafkaProperties) {
        topic = kafkaProperties.getProperties().get("topic");
        this.producer = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(kafkaProperties.buildProducerProperties(),
                                                                              new StringSerializer(),
                                                                              new StringSerializer()));
    }

    @Override
    public void publishEvent(String event) {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, event);
        producer.send(record);
    }

    @Override
    public void close() {
        producer.destroy();
    }
}

