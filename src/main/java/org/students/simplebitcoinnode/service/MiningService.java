package org.students.simplebitcoinnode.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.students.simplebitcoinnode.dto.DistributedMiningRequestDTO;
import org.students.simplebitcoinnode.entity.AdjacentNode;
import org.students.simplebitcoinnode.repository.AdjacentNodeRepository;

import java.util.logging.Logger;

@Service
public class MiningService {
    Logger logger = Logger.getLogger(NodeRegistrationService.class.getName());

    private final AdjacentNodeRepository adjacentNodeRepository;

    public MiningService(AdjacentNodeRepository adjacentNodeRepository) {
        this.adjacentNodeRepository = adjacentNodeRepository;
    }

    public void mining(DistributedMiningRequestDTO miningRequestDTO, HttpServletRequest httpServletRequest){
        // Check if ip/hostname exists.
        adjacentNodeRepository.existsByIp()
        httpServletRequest.getRemoteAddr();
    }
}
