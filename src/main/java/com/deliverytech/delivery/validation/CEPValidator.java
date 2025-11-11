package com.deliverytech.delivery.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * Implementação da lógica de validação para a anotação @ValidCEP.
 * <p>
 * Verifica se uma String (representando um CEP) está no formato
 * "12345678" ou "12345-678".
 */
public class CEPValidator implements ConstraintValidator<ValidCEP, String> {

    /**
     * Regex (Expressão Regular) para validar o formato do CEP.
     * Aceita 5 dígitos, um hífen opcional (-?), e 3 dígitos.
     */
    private static final Pattern CEP_PATTERN = Pattern.compile("^\\d{5}-?\\d{3}$");

    /**
     * Inicializa o validador.
     * (Nenhuma inicialização customizada é necessária aqui).
     */
    @Override
    public void initialize(ValidCEP constraintAnnotation) {
        // Nenhuma inicialização necessária
    }

    /**
     * Executa a lógica de validação do CEP.
     *
     * @param cep O valor (String) do CEP a ser validado.
     * @param context O contexto no qual a restrição é avaliada.
     * @return true se o CEP for válido, false caso contrário.
     */
    @Override
    public boolean isValid(String cep, ConstraintValidatorContext context) {
        // 1. Se o CEP for nulo ou vazio (após trim), é inválido.
        if (cep == null || cep.trim().isEmpty()) {
            return false;
        }
        
        // 2. Limpa o CEP (remove espaços em branco extras, caso haja)
        String cleanCep = cep.trim().replaceAll("\\s", "");

        // 3. Verifica se o CEP limpo corresponde ao padrão (Regex)
        return CEP_PATTERN.matcher(cleanCep).matches();
    }
}