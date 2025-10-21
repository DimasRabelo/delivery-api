package com.deliverytech.delivery.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class CEPValidator implements ConstraintValidator<ValidCEP, String> {

    // Regex para CEP: aceita "12345-678" ou "12345678"
    private static final Pattern CEP_PATTERN = Pattern.compile("^\\d{5}-?\\d{3}$");

    @Override
    public void initialize(ValidCEP constraintAnnotation) {
        // Inicialização se necessária
    }

    @Override
    public boolean isValid(String cep, ConstraintValidatorContext context) {
        if (cep == null || cep.trim().isEmpty()) {
            return false;
        }
        // Remove espaços em branco
        String cleanCep = cep.trim().replaceAll("\\s", "");
        return CEP_PATTERN.matcher(cleanCep).matches();
    }
}
