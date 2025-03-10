package com.devteria.Demo_Spring_boot.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Objects;
import java.util.regex.Pattern;

public class EmailValidator implements ConstraintValidator<EmailConstraint, String> {
    private static final Pattern GMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@gmail\\.com$");

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context){
        if (Objects.isNull(email) || email.isBlank()) {
            return false;
        }
        return GMAIL_PATTERN.matcher(email.trim()).matches();
    }
}
