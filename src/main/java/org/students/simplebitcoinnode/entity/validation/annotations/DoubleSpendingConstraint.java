package org.students.simplebitcoinnode.entity.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.students.simplebitcoinnode.entity.validation.DoubleSpendingConstraintValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DoubleSpendingConstraintValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface DoubleSpendingConstraint {
    String message() default "Invalid transaction inputs, TXO's either don't exist or they are already spent";
    Class<?>[] groups() default{};
    Class<? extends Payload>[] payload() default {};
}
