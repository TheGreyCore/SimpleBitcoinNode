package org.students.simplebitcoinnode.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.students.simplebitcoinnode.dto.PoolInitiationBlockMetadataDTO;
import org.students.simplebitcoinnode.dto.PoolMiningProposalDTO;
import org.students.simplebitcoinnode.entity.Block;
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
    public ResponseEntity<?> propose(@Valid @RequestBody PoolMiningProposalDTO proposalDTO) {
        try {
            miningService.propose(proposalDTO);
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.badRequest().body(illegalArgumentException);
        }
        return ResponseEntity.ok().build();
    }

    /**
     * This method is a POST mapping that initiates a block for mining.
     *
     * @param poolInitiationBlockMetadataDTO The data transfer object containing the metadata for the block initiation.
     * @return A ResponseEntity containing the initiated block if successful, or an error message if an exception occurs.
     */
    @PostMapping("/initiate")
    public ResponseEntity<?> initiate(@RequestBody PoolInitiationBlockMetadataDTO poolInitiationBlockMetadataDTO){
        try {
            Block initiatedBlock = miningService.initiate(poolInitiationBlockMetadataDTO);
            return ResponseEntity.ok().body(initiatedBlock);
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.badRequest().body(illegalArgumentException);
        } catch (Exception ignored){
            return ResponseEntity.internalServerError().body("An internal server error occurred.");
        }
    }
}