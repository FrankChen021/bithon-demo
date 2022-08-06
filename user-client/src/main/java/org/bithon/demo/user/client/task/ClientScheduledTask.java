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
import org.bithon.demo.user.api.ChangePasswordRequest;
import org.bithon.demo.user.api.IUserApi;
import org.bithon.demo.user.api.RegisterUserRequest;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Service
public class ClientScheduledTask {
    private final IUserApi userApi;

    public ClientScheduledTask(Contract contract,
                               Encoder encoder,
                               Decoder decoder,
                               Environment env) {
        userApi = Feign.builder()
                       .contract(contract)
                       .encoder(encoder)
                       .decoder(decoder)
                       .target(IUserApi.class,
                               String.format("http://%s",
                                             env.getProperty("bithon.demo.user-client.apiHost", "localhost:29525")));
    }

    private ArrayList<String> uids = new ArrayList<>();

    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.SECONDS)
    public void test() {
        String name = "user" + Long.toHexString(ThreadLocalRandom.current().nextLong()).substring(4);
        String password = Long.toHexString(ThreadLocalRandom.current().nextLong());

        String uid = userApi.register(RegisterUserRequest.builder()
                                                         .userName(name)
                                                         .password(password)
                                                         .build())
                            .getUid();

        String oldPassword = password;
        for (int i = 0; i < 3; i++) {
            String newPassword = Long.toHexString(ThreadLocalRandom.current().nextLong());
            userApi.changePassword(ChangePasswordRequest.builder()
                                                        .userName(name)
                                                        .oldPassword(oldPassword)
                                                        .newPassword(newPassword)
                                                        .build());

            oldPassword = newPassword;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }

        userApi.getProfileRequest(uid);

        uids.add(uid);
    }

    @Scheduled(fixedRate = 30, timeUnit = TimeUnit.SECONDS)
    public void testException() {
        try {
            // change password with wrong old password
            userApi.changePassword(ChangePasswordRequest.builder()
                                                        .userName("not_exist")
                                                        .oldPassword("wrong")
                                                        .newPassword("correct")
                                                        .build());
        } catch (Exception ignored) {
        }
    }

    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.MINUTES)
    public void clean() {
        ArrayList<String> toDeletes = uids;
        uids = new ArrayList<>();

        userApi.unregister(toDeletes);
    }
}
