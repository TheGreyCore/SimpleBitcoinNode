package org.students.simplebitcoinnode.entity.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.students.simplebitcoinnode.entity.validation.OneExternalRecipientPerTransactionConstraintValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OneExternalRecipientPerTransactionConstraintValidator.class)
public @interface OneExternalRecipientPerTransactionConstraint {
    String message() default "Only one external recipient is allowed in transaction outputs";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
