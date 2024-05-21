package org.students.simplebitcoinnode.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;

@Configuration
@ConfigurationProperties(prefix = "blockchain.mining")
@Getter
@Setter
public class BlockchainMiningConfig {
    private Boolean enabled;
    private Long minedBlockZeroBitCount;
    private Integer threadCount;
    private Integer transactionsPerBlock;
    private BlockchainMiningPoolConfig pool;
    private String blockConstructionCron;
    private String rewardAddress;

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newHttpClient();
    }
}
