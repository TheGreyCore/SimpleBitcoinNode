package org.students.simplebitcoinnode.service;

import org.students.simplebitcoinnode.dto.BlockIntroductionDTO;
import org.students.simplebitcoinnode.entity.AdjacentNode;

import java.io.IOException;
import java.util.List;

/**
 * Helper service that helps to construct mining pool
 */
public interface PoolFinderService {
    /**
     * Proposes adjacent nodes with a pool mining request and returns a list of adjacent nodes that accepted the proposal
     * @param blockIntroductionDTO specifies the block introduction object that will be used to introduce the block to different nodes
     * @return a list of nodes that accepted the proposal
     */
    List<AdjacentNode> proposePoolMining(BlockIntroductionDTO blockIntroductionDTO);

    /**
     * Send pool mining initiation request to nodes that accepted pool mining proposal
     * @param poolNodes specifies the list of nodes that accepted the proposal
     * @param blockHash represents the original hash of the block to mine
     */
    void initiatePoolMining(List<AdjacentNode> poolNodes, String blockHash) throws IOException;
}
