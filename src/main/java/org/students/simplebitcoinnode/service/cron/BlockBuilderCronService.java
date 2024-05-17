package org.students.simplebitcoinnode.service.cron;

import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.students.simplebitcoinnode.config.BlockchainMiningConfig;
import org.students.simplebitcoinnode.dto.BlockIntroductionDTO;
import org.students.simplebitcoinnode.entity.MerkleTreeNode;
import org.students.simplebitcoinnode.entity.Transaction;
import org.students.simplebitcoinnode.exceptions.encoding.InvalidEncodedStringException;
import org.students.simplebitcoinnode.repository.TransactionRepository;
import org.students.simplebitcoinnode.service.BlockBuilderService;

import java.util.List;
import java.util.logging.Logger;

/**
 * Cron job responsible for building the block and, if configured, mining it
 */
@Service
public class BlockBuilderCronService {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(BlockBuilderCronService.class);
    private final Logger logger = Logger.getLogger(BlockBuilderCronService.class.getName());

    private final BlockBuilderService blockBuilderService;
    private final TransactionRepository transactionRepository;
    private final BlockchainMiningConfig blockchainMiningConfig;

    // TODO: Inject adjacent node repository
    public BlockBuilderCronService(BlockBuilderService blockBuilderService,
                                   TransactionRepository transactionRepository,
                                   BlockchainMiningConfig blockchainMiningConfig) {
        this.blockBuilderService = blockBuilderService;
        this.transactionRepository = transactionRepository;
        this.blockchainMiningConfig = blockchainMiningConfig;
    }


    @Scheduled(cron = "${blockchain.mining.block-construction-cron}")
    public void scheduledBlockBuildingAndMining() {
        long unverified = transactionRepository.findAmountOfUnverifiedTransactions();
        if (unverified >= blockchainMiningConfig.getTransactionsPerBlock()) {
            try {
                List<Transaction> transactions = transactionRepository.findUnverifiedTransactionsLimitByN(blockchainMiningConfig.getTransactionsPerBlock());
                MerkleTreeNode root = blockBuilderService.calculateMerkleTreeRoot(transactions);

            }
            catch (InvalidEncodedStringException e) {
                logger.severe("Failed to build a merkle tree from transactions: " + e.getMessage());
                logger.severe("This could indicate a severe problem with transaction processing");
            }
        }
    }

    private List<String> initiatePoolMining(BlockIntroductionDTO blockIntroductionDTO) {
        // TODO: Introduce given block to other adjacent nodes for pool mining and gather all public keys
        //   of nodes who agreed to pool mining given block
        return List.of(blockchainMiningConfig.getRewardAddress());
    }

    private BlockIntroductionDTO constructBlock(MerkleTreeNode merkleTreeNode) {
        // TODO: Implement logic for constructing a new block
        return null;
    }
}
