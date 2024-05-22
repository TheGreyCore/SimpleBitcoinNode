package org.students.simplebitcoinnode;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;
import org.students.simplebitcoinnode.entity.*;
import org.students.simplebitcoinnode.repository.BlockRepository;
import org.students.simplebitcoinnode.repository.TransactionRepository;
import org.students.simplebitcoinnode.service.AsymmetricCryptographyService;
import org.students.simplebitcoinnode.util.Encoding;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@SpringBootApplication
@Profile("GenesisBlockMaker")
public class GenesisBlockMaker implements CommandLineRunner {
    private final BlockRepository blockRepository;
    private final TransactionRepository transactionRepository;
    private final AsymmetricCryptographyService asymmetricCryptographyService;
    private final Logger logger = Logger.getLogger(GenesisBlockMaker.class.getName());

    public GenesisBlockMaker(BlockRepository blockRepository, TransactionRepository transactionRepository, AsymmetricCryptographyService asymmetricCryptographyService) {
        this.blockRepository = blockRepository;
        this.transactionRepository = transactionRepository;
        this.asymmetricCryptographyService = asymmetricCryptographyService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (blockRepository.count() == 0) {
            Block block = Block.builder()
                    .blockAssemblyTimestamp(LocalDateTime.parse("2024-05-22T23:00:40"))
                    .hash("00000011db1bfb15937b6a81c7cc95c3edcfab8a1976f79722743d0842624b0a")
                    .minedTimestamp(LocalDateTime.parse("2024-05-22T23:01:32"))
                    .nonce(BigInteger.valueOf(13435435))
                    .previousHash("0".repeat(64))
                    .merkleTree(MerkleTreeNode.builder().hash("0".repeat(64)).build())
                    .miners(List.of(MinerPublicKey.builder().pubKey("PZ8Tyr4Nx8MHsRAGMpZmZ6TWY63dXWSCzGyThs339cjyNikASTvfrCEiY2nWjUis5PUCyF24R4nLYR8RYmkwGQ2ZAoH1L45RADpkSWZ7S6i7dKkDkgHkYVsB").build()))
                    .build();

            logger.info("Provided block hash: " + block.getHash());
            final String actualBlockHash = Encoding.toHexString(asymmetricCryptographyService.digestObject(block));
            logger.info("Actual block hash: " + actualBlockHash);

            Transaction coinbase = Transaction.builder()
                            .inputs(new ArrayList<>())
                            .outputs(List.of(
                                TransactionOutput.builder()
                                        .signature("")
                                        .amount(BigDecimal.valueOf(50))
                                        .receiverPublicKey("PZ8Tyr4Nx8MHsRAGMpZmZ6TWY63dXWSCzGyThs339cjyNikASTvfrCEiY2nWjUis5PUCyF24R4nLYR8RYmkwGQ2ZAoH1L45RADpkSWZ7S6i7dKkDkgHkYVsB")
                                        .build()
                            ))
                            .senderPublicKey("")
                            .timestamp(LocalDateTime.parse("2024-05-22T23:00:40"))
                            .build();
            coinbase.setTransactionHash(Encoding.toHexString(asymmetricCryptographyService.digestObject(coinbase)));
            transactionRepository.save(coinbase);
            blockRepository.save(block);
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(GenesisBlockMaker.class, args);
    }
}
