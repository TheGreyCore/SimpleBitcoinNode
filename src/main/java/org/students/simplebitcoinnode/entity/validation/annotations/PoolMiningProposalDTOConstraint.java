package org.students.simplebitcoinnode.entity.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.students.simplebitcoinnode.entity.validation.PoolMiningProposalDTOConstraintValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PoolMiningProposalDTOConstraintValidator.class)
public @interface PoolMiningProposalDTOConstraint {
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
