package org.students.simplebitcoinnode.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.students.simplebitcoinnode.dto.AdjacentNodeDTO;
import org.students.simplebitcoinnode.service.NodeRegistrationService;

@RestController
@RequestMapping("/blockchain/node/")
public class NodeRegistrationController {
    private final NodeRegistrationService nodeRegistrationService;

    public NodeRegistrationController(NodeRegistrationService nodeRegistrationService) {
        this.nodeRegistrationService = nodeRegistrationService;
    }

    /**
     * Registers a new node in the blockchain network.
     * Mapped to the "/register" endpoint and only allows POST requests.
     *
     * @param adjacentNodeDTO the data transfer object containing the adjacent node's information
     * @param request the HttpServletRequest
     * @return ResponseEntity with a message indicating the result of the registration
     */
    @PostMapping("/register")
    private ResponseEntity<?> registerNode(@Valid @RequestBody AdjacentNodeDTO adjacentNodeDTO, HttpServletRequest request){
        try{
            nodeRegistrationService.newNodeRegistration(adjacentNodeDTO, request);
        } catch (DataIntegrityViolationException e)
        {
            return ResponseEntity.badRequest().body("Node was not saved due to data integrity violation.");
        }
        return ResponseEntity.ok().body("Node was saved!");
    }
}
