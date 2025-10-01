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
import org.bithon.demo.user.api.GetProfileResponse;
import org.bithon.demo.user.api.IUserApi;
import org.bithon.demo.user.api.RegisterUserRequest;
import org.bithon.demo.user.api.RegisterUserResponse;
import org.springframework.core.env.Environment;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ClientScheduledTask {
    /**
     * client api backed by different http client implementations, just for test purpose
     */
    private final List<IUserApi> userApiList = new ArrayList<>();
    private final AtomicInteger userApiIndex = new AtomicInteger(0);
    private ArrayList<String> uids = new ArrayList<>();

    public ClientScheduledTask(Contract contract,
                               Encoder encoder,
                               Decoder decoder,
                               Environment env) {
        String endpoint = String.format(
            "http://%s",
            env.getProperty("bithon.demo.user-client.apiHost", "localhost:29525")
        );
        userApiList.add(Feign.builder()
                             .contract(contract)
                             .encoder(encoder)
                             .decoder(decoder)
                             .target(IUserApi.class, endpoint));

        // add a Spring RestClient which is based o java.net.http.HttpClient based implementation
        userApiList.add(new IUserApi() {
            final RestClient client = RestClient.builder()
                                                .requestFactory(new JdkClientHttpRequestFactory())
                                                .baseUrl(endpoint)
                                                .build();

            @Override
            public RegisterUserResponse register(RegisterUserRequest request) {
                return client
                    .post()
                    .uri("/api/user/register")
                    .header("Content-Type", "application/json")
                    .body(request)
                    .retrieve()
                    .body(RegisterUserResponse.class);
            }

            @Override
            public void changePassword(ChangePasswordRequest request) {
            }

            @Override
            public GetProfileResponse getProfile(String uid) {
                return client
                    .get()
                    .uri(String.format("/api/user/getProfile/%s", uid))
                    .retrieve()
                    .body(GetProfileResponse.class);
            }

            @Override
            public GetProfileResponse getProfile(String userName, String password) {
                return null;
            }

            @Override
            public void unregister(ArrayList<String> uids) {
            }

            @Override
            public List<String> showLogs() {
                return List.of();
            }
        });
    }

    private IUserApi nextUserApi() {
        return userApiList.get(userApiIndex.getAndIncrement() % userApiList.size());
    }

    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.SECONDS)
    public void test() {
        String name = "user" + Long.toHexString(ThreadLocalRandom.current().nextLong()).substring(4);
        String password = Long.toHexString(ThreadLocalRandom.current().nextLong());

        IUserApi userApi = nextUserApi();
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

        userApi.getProfile(uid);

        uids.add(uid);
    }

    @Scheduled(fixedRate = 30, timeUnit = TimeUnit.SECONDS)
    public void testException() {
        IUserApi userApi = nextUserApi();
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

        nextUserApi().unregister(toDeletes);
    }
}
