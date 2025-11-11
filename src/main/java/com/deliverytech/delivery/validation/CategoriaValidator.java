package com.deliverytech.delivery.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;

/**
 * Implementação da lógica de validação para a anotação @ValidCategoria.
 * <p>
 * Verifica se uma String (representando uma categoria) está presente
 * em uma lista predefinida de categorias válidas.
 */
public class CategoriaValidator implements ConstraintValidator<ValidCategoria, String> {

    /**
     * Lista estática (hardcoded) de todas as categorias de restaurante
     * permitidas no sistema. A validação é case-insensitive (feita com toUpperCase).
     */
    private static final List<String> CATEGORIAS_VALIDAS = Arrays.asList(
            "BRASILEIRA", "ITALIANA", "JAPONESA", "CHINESA", "MEXICANA",
            "FAST_FOOD", "PIZZA", "HAMBURGUER", "SAUDAVEL", "VEGETARIANA",
            "VEGANA", "DOCES", "BEBIDAS", "LANCHES", "ACAI"
    );

    /**
     * Inicializa o validador.
     * (Nenhuma inicialização customizada é necessária aqui).
     */
    @Override
    public void initialize(ValidCategoria constraintAnnotation) {
        // Nenhuma inicialização necessária
    }

    /**
     * Executa a lógica de validação da categoria.
     *
     * @param categoria O valor (String) da categoria a ser validada.
     * @param context O contexto no qual a restrição é avaliada.
     * @return true se a categoria for válida, false caso contrário.
     */
    @Override
    public boolean isValid(String categoria, ConstraintValidatorContext context) {
        // 1. Se a categoria for nula ou vazia, é inválida.
        // (Para campos opcionais, a anotação @NotBlank deve ser removida do DTO)
        if (categoria == null || categoria.trim().isEmpty()) {
            return false;
        }

        // 2. Verifica se a categoria (em maiúsculas) existe na lista de válidas
        return CATEGORIAS_VALIDAS.contains(categoria.toUpperCase());
    }
}