package org.students.simplebitcoinnode.entity.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.students.simplebitcoinnode.entity.validation.TransactionHashConstraintValidator;

import java.lang.annotation.*;

@Constraint(validatedBy = TransactionHashConstraintValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface TransactionHashConstraint {
    String message() default "Mismatching transaction hash";
    Class<?>[] groups() default{};
    Class<? extends Payload>[] payload() default {};
}
