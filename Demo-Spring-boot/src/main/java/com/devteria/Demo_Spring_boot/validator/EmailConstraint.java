package com.devteria.Demo_Spring_boot.validator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {EmailValidator.class})// truyền vào class để thực hiện validate

public @interface EmailConstraint {
    String message() default "Email must be (@gmail.com)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
