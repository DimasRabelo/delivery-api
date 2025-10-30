package com.deliverytech.delivery.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotação para validar o formato do horário de funcionamento.
 * Espera uma string no formato "HH:mm-HH:mm".
 */
@Documented // Para incluir na documentação JavaDoc
@Constraint(validatedBy = HorarioFuncionamentoValidator.class) // Liga a anotação à classe de lógica
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER, ElementType.TYPE_USE }) // Onde a anotação pode ser usada
@Retention(RetentionPolicy.RUNTIME) // A anotação precisa estar disponível em tempo de execução
public @interface ValidHorarioFuncionamento {

    // Mensagem de erro padrão que será usada se a validação falhar
    String message() default "Formato inválido para horário de funcionamento. Use HH:mm-HH:mm.";

    // Permite agrupar validações (padrão vazio)
    Class<?>[] groups() default {};

    // Permite carregar informações extras na validação (padrão vazio)
    Class<? extends Payload>[] payload() default {};
}