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
import feign.okhttp.OkHttpClient;
import lombok.extern.slf4j.Slf4j;
import org.bithon.demo.system.api.ISystemApi;
import org.quartz.JobExecutionContext;
import org.springframework.core.env.Environment;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class LivenessDetectionJob extends QuartzJobBean {

    // The job object is a one-time object
    private static final AtomicInteger index = new AtomicInteger(0);

    private final List<ISystemApi> systemApisList = new ArrayList<>();

    public LivenessDetectionJob(Contract contract,
                                Encoder encoder,
                                Decoder decoder,
                                Environment env) {
        String target = String.format("http://%s",
                                      env.getProperty("bithon.demo.user-client.apiHost", "localhost:29525"));

        // Feign(okhttp by default)
        systemApisList.add(Feign.builder()
                                .client(new OkHttpClient())
                                .contract(contract)
                                .encoder(encoder)
                                .decoder(decoder)
                                .target(ISystemApi.class, target));

        // JDK URLConnection
        systemApisList.add((ISystemApi) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{ISystemApi.class}, (proxy, method, args) -> {
            RestTemplate rt = new RestTemplate();
            return rt.getForObject(target + "/api/system/ping", String.class);
        }));

        // Apache HttpComponents
        systemApisList.add((ISystemApi) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{ISystemApi.class}, (proxy, method, args) -> {
            RestTemplate rt = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
            return rt.getForObject(target + "/api/system/ping", String.class);
        }));
    }

    @Override
    protected void executeInternal(JobExecutionContext context) {
        int i = index.getAndIncrement() % systemApisList.size();
        systemApisList.get(i).ping();
    }
}