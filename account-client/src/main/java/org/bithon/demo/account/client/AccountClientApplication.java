package org.bithon.demo.account.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.bithon.demo.account.api.GetBalanceRequest;
import org.bithon.demo.account.api.GetBalanceResponse;
import org.bithon.demo.account.api.IAccountApiGrpc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@EnableScheduling
@SpringBootApplication
public class AccountClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountClientApplication.class, args);
    }

    @Scheduled(fixedRate = 10_000)
    public static void checkBalance() {
        String serviceAddress = System.getProperty("bithon.demo.account.service.address", "localhost");
        int servicePort = Integer.parseInt(System.getProperty("bithon.demo.account.service.port", "29626"));

        ManagedChannel channel = ManagedChannelBuilder.forAddress(serviceAddress, servicePort)
                                                      .usePlaintext()
                                                      .build();

        try {
            IAccountApiGrpc.IAccountApiBlockingStub stub = IAccountApiGrpc.newBlockingStub(channel);
            GetBalanceResponse response = stub.getBalance(GetBalanceRequest.newBuilder()
                                                                           .setUserName("Frank")
                                                                           .setPassword("123456789")
                                                                           .build());

            log.info("Balance: {}", response.getBalance());
        } finally {
            channel.shutdown();
        }
    }
}
