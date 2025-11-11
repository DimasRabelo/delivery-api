package com.deliverytech.delivery.validation;

import jakarta.validation.Constraint; 
import jakarta.validation.Payload; 
import java.lang.annotation.*;

/**
 * Anotação de validação customizada {@code @ValidTelefone}.
 * <p>
 * Usada para marcar campos (geralmente {@code String}) que devem ser
 * validados no formato de telefone (10 ou 11 dígitos, com ou sem máscara).
 * <p>
 * A lógica de validação é implementada pela classe {@link TelefoneValidator}.
 */
@Documented
@Constraint(validatedBy = TelefoneValidator.class) // Liga esta anotação à sua classe de lógica
@Target({ElementType.FIELD, ElementType.PARAMETER}) // Onde a anotação pode ser usada (campos ou parâmetros)
@Retention(RetentionPolicy.RUNTIME) // A anotação estará disponível em tempo de execução
public @interface ValidTelefone {

    /**
     * @return A mensagem de erro padrão que será usada se a validação falhar.
     */
    String message() default "Telefone deve ter formato válido (10 ou 11 dígitos)";

    /**
     * @return Os grupos de validação aos quais esta restrição pertence (padrão de Bean Validation).
     */
    Class<?>[] groups() default {};

    /**
     * @return A carga útil (payload) associada à restrição (padrão de Bean Validation).
     */
    Class<? extends Payload>[] payload() default {};
}