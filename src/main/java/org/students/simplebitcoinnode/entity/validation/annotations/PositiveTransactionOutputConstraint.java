package org.students.simplebitcoinnode.entity.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.students.simplebitcoinnode.entity.validation.PositiveTransactionOutputConstraintValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PositiveTransactionOutputConstraintValidator.class)
public @interface PositiveTransactionOutputConstraint {
    String message() default "One of transaction outputs is not a positive decimal";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
