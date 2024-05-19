package org.students.simplebitcoinnode.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.students.simplebitcoinnode.event.listener.MineWorker;
import org.students.simplebitcoinnode.repository.BlockRepository;
import org.students.simplebitcoinnode.service.AsymmetricCryptographyService;

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
}
