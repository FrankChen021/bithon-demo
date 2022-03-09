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

package org.bithon.demo.user.client.task;

import feign.Contract;
import feign.Feign;
import feign.codec.Decoder;
import feign.codec.Encoder;
import org.bithon.demo.user.api.RegisterUserRequest;
import org.bithon.demo.user.api.IUserApi;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class ClientTask {
    private final IUserApi userApi;

    public ClientTask(Contract contract,
                      Encoder encoder,
                      Decoder decoder,
                      Environment env) {
        userApi = Feign.builder()
                       .contract(contract)
                       .encoder(encoder)
                       .decoder(decoder)
                       .target(IUserApi.class,
                               String.format("http://%s:29525", env.getProperty("bithon.demo.user-client.apiHost", "localhost")));
    }

    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.SECONDS)
    public void test() {
        System.out.println(userApi.register(new RegisterUserRequest()).getUid());
    }
}
