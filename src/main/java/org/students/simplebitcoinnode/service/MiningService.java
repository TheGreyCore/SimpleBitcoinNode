package org.students.simplebitcoinnode.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.students.simplebitcoinnode.config.BlockchainMiningConfig;
import org.students.simplebitcoinnode.config.BlockchainMiningPoolConfig;
import org.students.simplebitcoinnode.dto.BlockIntroductionDTO;
import org.students.simplebitcoinnode.dto.DistributedMiningRequestDTO;
import org.students.simplebitcoinnode.dto.PoolMiningProposalDTO;
import org.students.simplebitcoinnode.entity.AdjacentNode;
import org.students.simplebitcoinnode.entity.Block;
import org.students.simplebitcoinnode.repository.AdjacentNodeRepository;
import org.students.simplebitcoinnode.repository.BlockRepository;
import org.students.simplebitcoinnode.util.DTOMapperWrapper;

import java.util.logging.Logger;

@Service
public class MiningService {
    Logger logger = Logger.getLogger(NodeRegistrationService.class.getName());

    private final DTOMapperWrapper dtoMapperWrapper;

    private final AdjacentNodeRepository adjacentNodeRepository;

    private final BlockchainMiningPoolConfig blockchainMiningPoolConfig;

    private final BlockRepository blockRepository;

    public MiningService(DTOMapperWrapper dtoMapperWrapper, AdjacentNodeRepository adjacentNodeRepository, BlockchainMiningPoolConfig blockchainMiningPoolConfig, BlockRepository blockRepository) {
        this.dtoMapperWrapper = dtoMapperWrapper;
        this.adjacentNodeRepository = adjacentNodeRepository;
        this.blockchainMiningPoolConfig = blockchainMiningPoolConfig;
        this.blockRepository = blockRepository;
    }
    
    /**
     * Proposes a new mining operation.
     *
     * @param proposalDTO The data transfer object containing the details of the proposal.
     * @throws IllegalArgumentException If the expected pool size is larger than the allowed size or if the previous block hash does not represent a block with the longest chain.
     */
    public void propose(PoolMiningProposalDTO proposalDTO){
        // Check that amount of expected pool size is lower than allowed one.
        Integer maximumPoolRequestsSize = blockchainMiningPoolConfig.getMaximumPoolRequests();
        Integer expectedPoolSize = proposalDTO.getExpectedPoolSize();
        if (maximumPoolRequestsSize < expectedPoolSize) throw new IllegalArgumentException("Expected pool size is bigger than allowed size. Allowed size is: " + maximumPoolRequestsSize);

        // Check that the previous block hash represents a block with the longest chain.
        String blockWithLongestChainHash= blockRepository.findBlockWithLongestChain().getHash();
        String previousBlockHash = proposalDTO.getBlock().getPreviousHash();
        if (!previousBlockHash.equals(blockWithLongestChainHash)) throw new IllegalArgumentException("Passed previous block has is invalid!");

        // Save new block to the database
        blockRepository.save(dtoMapperWrapper.unmap(proposalDTO.getBlock(), Block.class));
    }
}
