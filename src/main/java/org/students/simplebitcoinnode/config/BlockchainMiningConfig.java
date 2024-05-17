package org.students.simplebitcoinnode.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "blockchain.mining")
@Getter
@Setter
public class BlockchainMiningConfig {
    private Boolean enabled;
    private Integer transactionsPerBlock;
    private BlockchainMiningPoolConfig pool;
    private String blockConstructionCron;
    private String rewardAddress;
}
