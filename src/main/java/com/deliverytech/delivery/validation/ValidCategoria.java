package com.deliverytech.delivery.validation;

import jakarta.validation.Constraint; 
import jakarta.validation.Payload; 
import java.lang.annotation.*;

/**
 * Anotação de validação customizada {@code @ValidCategoria}.
 * <p>
 * Usada para marcar campos (geralmente {@code String}) que devem ser
 * validados contra a lista predefinida de categorias de restaurante.
 * <p>
 * A lógica de validação é implementada pela classe {@link CategoriaValidator}.
 */
@Documented
@Constraint(validatedBy = CategoriaValidator.class) // Liga esta anotação à sua classe de lógica
@Target({ElementType.FIELD, ElementType.PARAMETER}) // Onde a anotação pode ser usada (campos ou parâmetros)
@Retention(RetentionPolicy.RUNTIME) // A anotação estará disponível em tempo de execução
public @interface ValidCategoria {

    /**
     * @return A mensagem de erro padrão que será usada se a validação falhar.
     */
    String message() default "Categoria deve ser uma das opções válidas";

    /**
     * @return Os grupos de validação aos quais esta restrição pertence (padrão de Bean Validation).
     */
    Class<?>[] groups() default {};

    /**
     * @return A carga útil (payload) associada à restrição (padrão de Bean Validation).
     */
    Class<? extends Payload>[] payload() default {};
}