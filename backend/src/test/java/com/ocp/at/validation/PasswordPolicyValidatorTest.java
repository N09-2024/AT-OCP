package com.ocp.at.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.mockito.Mockito;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordPolicyValidatorTest {

    private PasswordPolicyValidator validator;
    private ConstraintValidatorContext context;
    private ConstraintValidatorContext.ConstraintViolationBuilder builder;

    @BeforeEach
    void setUp() {
        validator = new PasswordPolicyValidator();
        context = Mockito.mock(ConstraintValidatorContext.class);
        builder = Mockito.mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        Mockito.lenient().when(context.buildConstraintViolationWithTemplate(Mockito.anyString())).thenReturn(builder);
        Mockito.lenient().when(builder.addConstraintViolation()).thenReturn(context);
        org.springframework.test.util.ReflectionTestUtils.setField(validator, "minLength", 8);
    }

    @Test
    void motDePasseValide_DoitRetournerTrue() {
        assertTrue(validator.isValid("Admin@1234", context));
        assertTrue(validator.isValid("MonP@ss1!", context));
    }

    @Test
    void sansMajuscule_DoitRetournerFalse() {
        assertFalse(validator.isValid("admin@1234", context));
    }

    @Test
    void sansMinuscule_DoitRetournerFalse() {
        assertFalse(validator.isValid("ADMIN@1234", context));
    }

    @Test
    void sansChiffre_DoitRetournerFalse() {
        assertFalse(validator.isValid("Admin@Test", context));
    }

    @Test
    void sansCaractereSpecial_DoitRetournerFalse() {
        assertFalse(validator.isValid("Admin1234", context));
    }

    @Test
    void tropCourt_DoitRetournerFalse() {
        assertFalse(validator.isValid("A@b1", context));
    }

    @Test
    void vide_DoitRetournerTrue() {
        // Le null/vide est géré par @NotBlank, pas par ce validateur
        assertTrue(validator.isValid(null, context));
        assertTrue(validator.isValid("", context));
    }
}
