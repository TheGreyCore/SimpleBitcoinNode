package org.students.simplebitcoinwallet.entity.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.students.simplebitcoinwallet.entity.validation.TransactionHashConstraintValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = TransactionHashConstraintValidator.class)
@Target({ ElementType.TYPE })
public @interface TransactionHashConstraint {
    String message() default "Mismatching transaction hash";
    Class<?>[] groups() default{};
    Class<? extends Payload>[] payload() default {};
}
