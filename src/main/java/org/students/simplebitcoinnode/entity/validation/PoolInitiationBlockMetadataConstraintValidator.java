package org.students.simplebitcoinnode.entity.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.students.simplebitcoinnode.dto.PoolInitiationBlockMetadataDTO;
import org.students.simplebitcoinnode.entity.Block;
import org.students.simplebitcoinnode.entity.validation.annotations.PositiveTransactionOutputConstraint;
import org.students.simplebitcoinnode.repository.BlockRepository;

public class PoolInitiationBlockMetadataConstraintValidator implements ConstraintValidator<PositiveTransactionOutputConstraint, PoolInitiationBlockMetadataDTO> {
    public final BlockRepository blockRepository;

    public PoolInitiationBlockMetadataConstraintValidator(BlockRepository blockRepository) {
        this.blockRepository = blockRepository;
    }

    /**
     * Initializes the validator.
     * @param constraintAnnotation The annotation instance for a given constraint declaration.
     */
    @Override
    public void initialize(PositiveTransactionOutputConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    /**
     * Checks if the provided PoolInitiationBlockMetadataDTO object is valid.
     * @param poolInitiationBlockMetadataDTO The DTO object to validate.
     * @param constraintValidatorContext Context in which the constraint is evaluated.
     * @return true if the object is valid, false otherwise.
     */
    @Override
    public boolean isValid(PoolInitiationBlockMetadataDTO poolInitiationBlockMetadataDTO, ConstraintValidatorContext constraintValidatorContext) {
        // Get block for initiating
        Block initiateBlock = blockRepository.findBlockHeaderByHash(poolInitiationBlockMetadataDTO.getHash()).orElse(null);

        // Check that data are correctly provided.
        if (initiateBlock == null) {
            constraintValidatorContext.buildConstraintViolationWithTemplate("Provided hash is not equal to the last block hash!")
                    .addConstraintViolation();
            return false;
        }
        if (poolInitiationBlockMetadataDTO.getMiners().isEmpty()) {
            constraintValidatorContext.buildConstraintViolationWithTemplate("Miners cannot be <= 0!")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
