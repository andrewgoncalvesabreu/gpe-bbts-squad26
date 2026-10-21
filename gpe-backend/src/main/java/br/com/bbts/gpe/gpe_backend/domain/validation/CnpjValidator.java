package br.com.bbts.gpe.gpe_backend.domain.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CnpjValidator implements ConstraintValidator<Cnpj, String> {
    private static final int[] FIRST_DIGIT_WEIGHTS = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] SECOND_DIGIT_WEIGHTS = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.length() != 14 || !value.matches("[0-9A-Z]{12}[0-9]{2}")) {
            return false;
        }
        if (value.chars().distinct().count() == 1) {
            return false;
        }

        int firstDigit = calculateDigit(value.substring(0, 12), FIRST_DIGIT_WEIGHTS);
        int secondDigit = calculateDigit(value.substring(0, 13), SECOND_DIGIT_WEIGHTS);
        return value.charAt(12) - '0' == firstDigit && value.charAt(13) - '0' == secondDigit;
    }

    private int calculateDigit(String base, int[] weights) {
        int sum = 0;
        for (int index = 0; index < weights.length; index++) {
            sum += (base.charAt(index) - '0') * weights[index];
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }
}
