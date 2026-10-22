package br.com.bbts.gpe.gpe_backend.domain.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CnpjValidatorTest {
    private final CnpjValidator validator = new CnpjValidator();

    @Test
    void acceptsValidNumericCnpj() {
        assertTrue(validator.isValid("11222333000181", null));
    }

    @Test
    void rejectsInvalidAndRepeatedCnpj() {
        assertFalse(validator.isValid("11222333000182", null));
        assertFalse(validator.isValid("00000000000000", null));
        assertFalse(validator.isValid(null, null));
    }
}
