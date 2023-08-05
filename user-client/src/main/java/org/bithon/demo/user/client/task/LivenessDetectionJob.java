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

import feign.Client;
import feign.Contract;
import feign.Feign;
import feign.codec.Decoder;
import feign.codec.Encoder;
import lombok.extern.slf4j.Slf4j;
import org.bithon.demo.system.api.ISystemApi;
import org.quartz.JobExecutionContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LivenessDetectionJob extends QuartzJobBean {

    private final ISystemApi systemApi;

    public LivenessDetectionJob(Client client,
                                Contract contract,
                                Encoder encoder,
                                Decoder decoder,
                                Environment env) {
        systemApi = Feign.builder()
                         .client(client)
                         .contract(contract)
                         .encoder(encoder)
                         .decoder(decoder)
                         .target(ISystemApi.class,
                                 String.format("http://%s",
                                               env.getProperty("bithon.demo.user-client.apiHost", "localhost:29525")));
    }

    @Override
    protected void executeInternal(JobExecutionContext context) {
        systemApi.ping();
    }
}