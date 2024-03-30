package org.students.simplebitcoinwallet.entity.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.students.simplebitcoinwallet.entity.validation.DoubleSpendingConstraintValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = DoubleSpendingConstraintValidator.class)
@Target({ ElementType.TYPE })
public @interface DoubleSpendingConstraint {
    String message() default "Invalid transaction inputs, TXO's either don't exist or they are already spent";
    Class<?>[] groups() default{};
    Class<? extends Payload>[] payload() default {};
}
