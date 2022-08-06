package org.bithon.demo.user.client.task;

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

    public LivenessDetectionJob(Contract contract,
                                Encoder encoder,
                                Decoder decoder,
                                Environment env) {
        systemApi = Feign.builder()
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