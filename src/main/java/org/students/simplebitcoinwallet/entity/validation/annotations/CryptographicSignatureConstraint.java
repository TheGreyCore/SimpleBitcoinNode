package org.students.simplebitcoinwallet.entity.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.students.simplebitcoinwallet.entity.validation.CryptographicSignatureConstraintValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = CryptographicSignatureConstraintValidator.class)
@Target({ ElementType.TYPE })
public @interface CryptographicSignatureConstraint {
    String message() default "Invalid cryptographic signature at transaction outputs";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
