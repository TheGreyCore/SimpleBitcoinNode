package org.students.simplebitcoinnode.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "blockchain.mining.pool")
@Getter
@Setter
public class BlockchainMiningPoolConfig {
    private Boolean enabled;
    private Integer maximumPoolRequests;
}