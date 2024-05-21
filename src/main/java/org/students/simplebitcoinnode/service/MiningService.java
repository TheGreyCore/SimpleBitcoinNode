package org.students.simplebitcoinnode.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.students.simplebitcoinnode.config.BlockchainMiningPoolConfig;
import org.students.simplebitcoinnode.dto.PoolInitiationBlockMetadataDTO;
import org.students.simplebitcoinnode.dto.PoolMiningProposalDTO;
import org.students.simplebitcoinnode.entity.Block;
import org.students.simplebitcoinnode.entity.MinerPublicKey;
import org.students.simplebitcoinnode.event.MineBlockEvent;
import org.students.simplebitcoinnode.repository.AdjacentNodeRepository;
import org.students.simplebitcoinnode.repository.BlockRepository;
import org.students.simplebitcoinnode.util.DTOMapperWrapper;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class MiningService {
    Logger logger = Logger.getLogger(NodeRegistrationService.class.getName());
    private final DTOMapperWrapper dtoMapperWrapper;
    private final AdjacentNodeRepository adjacentNodeRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final BlockchainMiningPoolConfig blockchainMiningPoolConfig;
    private final BlockRepository blockRepository;

    public MiningService(DTOMapperWrapper dtoMapperWrapper, AdjacentNodeRepository adjacentNodeRepository, ApplicationEventPublisher applicationEventPublisher, BlockchainMiningPoolConfig blockchainMiningPoolConfig, BlockRepository blockRepository) {
        this.dtoMapperWrapper = dtoMapperWrapper;
        this.adjacentNodeRepository = adjacentNodeRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.blockchainMiningPoolConfig = blockchainMiningPoolConfig;
        this.blockRepository = blockRepository;
    }

    /**
     * Proposes a new mining operation.
     *
     * @param proposalDTO The data transfer object containing the details of the proposal.
     * @throws IllegalArgumentException If the expected pool size is larger than the allowed size or if the previous block hash does not represent a block with the longest chain.
     */
    public void propose(PoolMiningProposalDTO proposalDTO) {
        // Save new block to the database
        blockRepository.save(dtoMapperWrapper.unmap(proposalDTO.getBlock(), Block.class));
    }

    /**
     * This method initiates a block for mining.
     *
     * @param poolInitiationBlockMetadataDTO The data transfer object containing the metadata for the block initiation.
     * @return The initiated block ready for mining.
     * @throws IllegalArgumentException If the block of the given hash is not found or if the number of miners is zero.
     */
    public Block initiate(PoolInitiationBlockMetadataDTO poolInitiationBlockMetadataDTO) {
        // Get block for initiating
        Block initiateBlock = blockRepository.findBlockHeaderByHash(poolInitiationBlockMetadataDTO.getHash()).orElse(null);

        // Get list of miners publicKeys and modify block
        List<MinerPublicKey> minerPublicKeyList = new ArrayList<>();
        for (String minerString : poolInitiationBlockMetadataDTO.getMiners()) {
            MinerPublicKey minerPublicKey = new MinerPublicKey();
            minerPublicKey.setPubKey(minerString);
            minerPublicKeyList.add(minerPublicKey);
        }

        assert initiateBlock != null;
        initiateBlock.setMiners(minerPublicKeyList);

        // Publish new MineBlockEvent
        BigInteger offset = BigInteger.valueOf(poolInitiationBlockMetadataDTO.getOffset());
        BigInteger stride = BigInteger.valueOf(minerPublicKeyList.size());
        MineBlockEvent mineBlockEvent = new MineBlockEvent(MiningService.class, initiateBlock, offset, stride);
        applicationEventPublisher.publishEvent(mineBlockEvent);

        return initiateBlock;
    }
}
