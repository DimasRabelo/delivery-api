package com.deliverytech.delivery.validation;

import jakarta.validation.Constraint; 
import jakarta.validation.Payload; 
import java.lang.annotation.*;

/**
 * Anotação de validação customizada {@code @ValidCEP}.
 * <p>
 * Usada para marcar campos (geralmente {@code String}) que devem ser
 * validados no formato de CEP (ex: "00000000" ou "00000-000").
 * <p>
 * A lógica de validação é implementada pela classe {@link CEPValidator}.
 */
@Documented
@Constraint(validatedBy = CEPValidator.class) // Liga esta anotação à sua classe de lógica
@Target({ElementType.FIELD, ElementType.PARAMETER}) // Onde a anotação pode ser usada (campos ou parâmetros)
@Retention(RetentionPolicy.RUNTIME) // A anotação estará disponível em tempo de execução
public @interface ValidCEP {

    /**
     * @return A mensagem de erro padrão que será usada se a validação falhar.
     */
    String message() default "CEP deve ter formato válido (00000-000 ou 00000000)";

    /**
     * @return Os grupos de validação aos quais esta restrição pertence (padrão de Bean Validation).
     */
    Class<?>[] groups() default {};

    /**
     * @return A carga útil (payload) associada à restrição (padrão de Bean Validation).
     */
    Class<? extends Payload>[] payload() default {};
}