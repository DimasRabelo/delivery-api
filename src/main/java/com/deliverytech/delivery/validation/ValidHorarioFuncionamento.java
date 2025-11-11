package com.deliverytech.delivery.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotação de validação customizada {@code @ValidHorarioFuncionamento}.
 * <p>
 * Usada para marcar campos (geralmente {@code String}) que devem ser
 * validados no formato "HH:mm-HH:mm".
 * <p>
 * A lógica de validação é implementada pela classe {@link HorarioFuncionamentoValidator}.
 */
@Documented // Indica que esta anotação deve ser incluída no Javadoc
@Constraint(validatedBy = HorarioFuncionamentoValidator.class) // Liga esta anotação à sua classe de lógica
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER, ElementType.TYPE_USE }) // Onde a anotação pode ser usada
@Retention(RetentionPolicy.RUNTIME) // A anotação estará disponível em tempo de execução
public @interface ValidHorarioFuncionamento {

    /**
     * @return A mensagem de erro padrão que será usada se a validação falhar.
     */
    String message() default "Formato inválido para horário de funcionamento. Use HH:mm-HH:mm.";

    /**
     * @return Os grupos de validação aos quais esta restrição pertence (padrão de Bean Validation).
     */
    Class<?>[] groups() default {};

    /**
     * @return A carga útil (payload) associada à restrição (padrão de Bean Validation).
     */
    Class<? extends Payload>[] payload() default {};
}