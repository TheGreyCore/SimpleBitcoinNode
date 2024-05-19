package org.students.simplebitcoinnode.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.students.simplebitcoinnode.dto.PoolMiningProposalDTO;
import org.students.simplebitcoinnode.service.MiningService;

@RestController
@RequestMapping("/blockchain/mine/")
public class MiningController {

    private final MiningService miningService;

    public MiningController(MiningService miningService) {
        this.miningService = miningService;
    }

    /**
     * Endpoint for proposing a new mining operation.
     *
     * @param proposalDTO The data transfer object containing the details of the proposal.
     * @return ResponseEntity with a message indicating the result of the operation.
     */
    @PostMapping("/propose")
    public ResponseEntity<?> propose (@RequestBody PoolMiningProposalDTO proposalDTO){
        try {
            miningService.propose(proposalDTO);
        } catch (IllegalArgumentException illegalArgumentException){
            return ResponseEntity.badRequest().body(illegalArgumentException);
        }
        return ResponseEntity.ok().body("New propose saved.");
