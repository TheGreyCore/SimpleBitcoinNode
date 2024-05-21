package org.students.simplebitcoinnode.entity.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.students.simplebitcoinnode.entity.validation.PoolInitiationBlockMetadataConstraintValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PoolInitiationBlockMetadataConstraintValidator.class)
public @interface PoolInitiationBlockMetadataConstraint {
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
