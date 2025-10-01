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
import feign.Feign;
import feign.codec.Decoder;
import feign.codec.Encoder;
import io.grpc.stub.StreamObserver;
import org.bithon.demo.account.api.GetBalanceRequest;
import org.bithon.demo.account.api.GetBalanceResponse;
import org.bithon.demo.account.api.IAccountApiGrpc;
import org.bithon.demo.user.api.IUserApi;
import org.springframework.core.env.Environment;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author frank.chen021@outlook.com
 * @date 2022/12/24 16:17
 */
public class AccountServiceImpl extends IAccountApiGrpc.IAccountApiImplBase {

    private final IUserApi userApi;
    private final AtomicLong balance = new AtomicLong();

    public AccountServiceImpl(Contract contract,
                              Encoder encoder,
                              Decoder decoder,
                              Environment env) {
        userApi = Feign.builder()
                       .contract(contract)
                       .encoder(encoder)
                       .decoder(decoder)
                       .target(IUserApi.class,
                               String.format("http://%s",
                                             env.getProperty("bithon.demo.user-client.apiHost", "localhost:29526")));
    }

    @Override
    public void getBalance(GetBalanceRequest request, StreamObserver<GetBalanceResponse> responseObserver) {
        userApi.getProfile(request.getUserName(), request.getPassword());

        responseObserver.onNext(GetBalanceResponse.newBuilder().setBalance(balance.incrementAndGet()).build());
        responseObserver.onCompleted();
    }
}
