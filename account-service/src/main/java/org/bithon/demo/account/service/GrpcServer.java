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

package org.bithon.demo.account.service;

import feign.Contract;
import feign.codec.Decoder;
import feign.codec.Encoder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author frank.chen021@outlook.com
 * @date 2022/12/24 16:34
 */
@Slf4j
@Component
public class GrpcServer implements CommandLineRunner, ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Override
    public void run(String... args) throws IOException, InterruptedException {
        Environment env = applicationContext.getBean(Environment.class);
        int port = env.getProperty("bithon.demo.account.service.port", Integer.class, 29626);
        Server server = ServerBuilder.forPort(port)
                                     .addService(new AccountServiceImpl(applicationContext.getBean(Contract.class),
                                                                        applicationContext.getBean(Encoder.class),
                                                                        applicationContext.getBean(Decoder.class),
                                                                        env))
                                     .build();

        log.info("Starting gRPC server on port: {}", port);
        server.start().awaitTermination();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
