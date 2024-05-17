package org.students.simplebitcoinnode.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.students.simplebitcoinnode.dto.AdjacentNodeDTO;
import org.students.simplebitcoinnode.entity.AdjacentNode;
import org.students.simplebitcoinnode.repository.AdjacentNodeRepository;
import org.students.simplebitcoinnode.util.DTOMapperWrapper;

import java.util.logging.Logger;

@Service
public class NodeRegistrationService {
    Logger logger = Logger.getLogger(NodeRegistrationService.class.getName());
    private final AdjacentNodeRepository adjacentNodeRepository;
    private final DTOMapperWrapper dtoMapperWrapper;

    /**
     * Constructs a new NodeRegistrationService with the specified AdjacentNodeRepository and DTOMapperWrapper.
     *
     * @param adjacentNodeRepository the repository for storing adjacent node data
     * @param dtoMapperWrapper the wrapper for mapping between DTOs and entities
     */
    public NodeRegistrationService(AdjacentNodeRepository adjacentNodeRepository, DTOMapperWrapper dtoMapperWrapper) {
        this.adjacentNodeRepository = adjacentNodeRepository;
        this.dtoMapperWrapper = dtoMapperWrapper;
    }

    /**
     * Registers a new node in the blockchain network.
     *
     * @param adjacentNodeDTO the data transfer object containing the adjacent node's information
     * @param request the HttpServletRequest
     * @throws DataIntegrityViolationException if a data integrity violation occurs when saving the new adjacent node
     */
    public void newNodeRegistration(AdjacentNodeDTO adjacentNodeDTO, HttpServletRequest request){
        AdjacentNode adjacentNode = dtoMapperWrapper.unmap(adjacentNodeDTO, AdjacentNode.class);
        try {
            adjacentNode.setIp(request.getRemoteAddr());
            adjacentNodeRepository.save(adjacentNode);
        } catch (DataIntegrityViolationException e) {
            logger.warning("An data integrity violation exception occurred when client tried save new adjacent node: " + e.getMessage());
            throw e;
        }
    }
}
