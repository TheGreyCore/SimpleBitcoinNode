package org.students.simplebitcoinnode.entity.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.students.simplebitcoinnode.entity.validation.CryptographicSignatureConstraintValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CryptographicSignatureConstraintValidator.class)
public @interface CryptographicSignatureConstraint {
    String message() default "Invalid cryptographic signature at transaction outputs";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
