package org.students.simplebitcoinnode.entity.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.students.simplebitcoinnode.config.BlockchainMiningPoolConfig;
import org.students.simplebitcoinnode.dto.PoolMiningProposalDTO;
import org.students.simplebitcoinnode.entity.validation.annotations.PoolMiningProposalDTOConstraint;
import org.students.simplebitcoinnode.repository.BlockRepository;

public class PoolMiningProposalDTOConstraintValidator implements ConstraintValidator<PoolMiningProposalDTOConstraint, PoolMiningProposalDTO> {
    private final BlockchainMiningPoolConfig blockchainMiningPoolConfig;

    private final BlockRepository blockRepository;

    public PoolMiningProposalDTOConstraintValidator(BlockchainMiningPoolConfig blockchainMiningPoolConfig, BlockRepository blockRepository) {
        this.blockchainMiningPoolConfig = blockchainMiningPoolConfig;
        this.blockRepository = blockRepository;
    }

    @Override
    public void initialize(PoolMiningProposalDTOConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    /**
     * This method validates a given mining proposal.
     *
     * @param proposalDTO The mining proposal to be validated.
     * @param constraintValidatorContext The context in which the constraint is evaluated.
     * @return true if the mining proposal is valid, false otherwise.
     *
     * The method performs two checks:
     * 1. It checks if the expected pool size is less than or equal to the maximum allowed pool size.
     *    If the expected pool size is greater, it builds a constraint violation and returns false.
     * 2. It checks if the previous block hash in the proposal matches the hash of the block with the longest chain.
     *    If the hashes do not match, it builds a constraint violation and returns false.
     */
    @Override
    public boolean isValid(PoolMiningProposalDTO proposalDTO, ConstraintValidatorContext constraintValidatorContext) {
        // Check that amount of expected pool size is lower than allowed one.
        Integer maximumPoolRequestsSize = blockchainMiningPoolConfig.getMaximumPoolRequests();
        Integer expectedPoolSize = proposalDTO.getExpectedPoolSize();
        if (maximumPoolRequestsSize < expectedPoolSize){
            constraintValidatorContext.buildConstraintViolationWithTemplate("Expected pool size is bigger than allowed size. Allowed size is: " + maximumPoolRequestsSize)
                    .addConstraintViolation();
            return false;
        }

        // Check that the previous block hash represents a block with the longest chain.
        String blockWithLongestChainHash = blockRepository.findBlockWithLongestChain().getHash();
        String previousBlockHash = proposalDTO.getBlock().getPreviousHash();
        if (!previousBlockHash.equals(blockWithLongestChainHash)){
            constraintValidatorContext.buildConstraintViolationWithTemplate("Passed previous block hash is invalid!");
            return false;
        }

        return true;
    }
}
