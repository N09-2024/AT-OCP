package com.ocp.at.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;

import java.util.regex.Pattern;

public class PasswordPolicyValidator implements ConstraintValidator<PasswordPolicy, String> {

    @Value("${app.security.password.min-length:8}")
    private int minLength;

    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]");

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isBlank()) {
            return true; // @NotBlank gère ce cas
        }

        boolean valid = password.length() >= minLength
                && UPPERCASE.matcher(password).find()
                && LOWERCASE.matcher(password).find()
                && DIGIT.matcher(password).find()
                && SPECIAL.matcher(password).find();

        if (!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Le mot de passe doit contenir au moins " + minLength +
                " caractères, une majuscule, une minuscule, un chiffre et un caractère spécial"
            ).addConstraintViolation();
        }

        return valid;
    }
}
