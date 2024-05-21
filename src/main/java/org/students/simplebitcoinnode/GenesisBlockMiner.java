package org.students.simplebitcoinnode;


import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.students.simplebitcoinnode.entity.Block;
import org.students.simplebitcoinnode.entity.MerkleTreeNode;
import org.students.simplebitcoinnode.entity.MinerPublicKey;
import org.students.simplebitcoinnode.event.BlockMinedEvent;
import org.students.simplebitcoinnode.event.MineBlockEvent;
import org.students.simplebitcoinnode.repository.BlockRepository;
import org.students.simplebitcoinnode.service.AsymmetricCryptographyService;
import org.students.simplebitcoinnode.util.Encoding;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.logging.Logger;

@Profile("GenesisMiner")
@SpringBootApplication
public class GenesisBlockMiner implements CommandLineRunner {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(GenesisBlockMiner.class);
    private final ApplicationEventPublisher applicationEventPublisher;
    private final AsymmetricCryptographyService asymmetricCryptographyService;
    private final BlockRepository blockRepository;

    private static String minerAddress = "PZ8Tyr4Nx8MHsRAGMpZmZ6TWY63dXWSCzGyThs339cjyNikASTvfrCEiY2nWjUis5PUCyF24R4nLYR8RYmkwGQ2ZAoH1L45RADpkSWZ7S6i7dKkDkgHkYVsB";
    private final Logger logger = Logger.getLogger(GenesisBlockMiner.class.getName());

    public GenesisBlockMiner(ApplicationEventPublisher applicationEventPublisher,
                             AsymmetricCryptographyService asymmetricCryptographyService,
                             BlockRepository blockRepository) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.asymmetricCryptographyService = asymmetricCryptographyService;
        this.blockRepository = blockRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        Block genesisBlock = Block.builder()
                .miners(List.of(new MinerPublicKey(null, minerAddress)))
                .nonce(BigInteger.ZERO)
                .merkleTree(MerkleTreeNode.builder().hash("0".repeat(64)).build())
                .previousHash("0".repeat(64))
                .blockAssemblyTimestamp(LocalDateTime.now(ZoneId.of("UTC")))
                .build();

        // find current block hash
        final String startHash = Encoding.toHexString(asymmetricCryptographyService.digestObject(genesisBlock));
        genesisBlock.setHash(startHash);
        logger.info("Starting mining a block with current hash '" + startHash + "'");
        applicationEventPublisher.publishEvent(new MineBlockEvent(this, genesisBlock, BigInteger.ZERO, BigInteger.ONE));
    }

    @EventListener
    public void onBlockMinedEvent(BlockMinedEvent event) {
        logger.info("Genesis block is mined with hash: '" + event.getBlock().getHash() + "'");
        event.getBlock().setMinedTimestamp(LocalDateTime.now(ZoneId.of("UTC")));
        blockRepository.save(event.getBlock());
        System.exit(0);
    }

    public static void main(String[] args) {
        SpringApplication.run(GenesisBlockMiner.class, args);
    }
}
