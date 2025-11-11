package com.deliverytech.delivery.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementa a lógica de validação para a anotação @ValidHorarioFuncionamento.
 * Verifica se a string está no formato "HH:mm-HH:mm" e se as horas/minutos são válidos.
 */
public class HorarioFuncionamentoValidator implements ConstraintValidator<ValidHorarioFuncionamento, String> {

    // Compila a expressão regular uma vez para melhor performance
    // Regex: \d{2} -> dois dígitos, : -> literal :, - -> literal -
    private static final Pattern HORARIO_PATTERN = Pattern.compile("^(\\d{2}):(\\d{2})-(\\d{2}):(\\d{2})$");

    @Override
    public void initialize(ValidHorarioFuncionamento constraintAnnotation) {
        // Método chamado ao inicializar o validador (pode ser usado para pegar atributos da anotação, se houver)
    }

    @Override
    public boolean isValid(String horario, ConstraintValidatorContext context) {
        // Se o horário for nulo ou vazio, considera válido (use @NotBlank/@NotNull para obrigatoriedade)
        if (horario == null || horario.trim().isEmpty()) {
            return true; // Ou false se o campo for obrigatório E validado aqui
        }

        Matcher matcher = HORARIO_PATTERN.matcher(horario);

        // 1. Verifica se o formato geral "HH:mm-HH:mm" bate
        if (!matcher.matches()) {
            return false;
        }

        try {
            // 2. Extrai as horas e minutos (matcher.group(index) pega os parênteses da regex)
            int horaInicio = Integer.parseInt(matcher.group(1));
            int minutoInicio = Integer.parseInt(matcher.group(2));
            int horaFim = Integer.parseInt(matcher.group(3));
            int minutoFim = Integer.parseInt(matcher.group(4));

            // 3. Verifica se as horas (00-23) e minutos (00-59) são válidos individualmente
            if (!isHoraValida(horaInicio) || !isMinutoValido(minutoInicio) ||
                !isHoraValida(horaFim) || !isMinutoValido(minutoFim)) {
                return false;
            }

            // 4. (Opcional) Poderia verificar se horaInicio < horaFim,
            // mas isso pode ser complexo (ex: 22:00-02:00).
            // Para simplificar, validamos apenas o formato e os valores.

            return true; // Se passou por todas as verificações

        } catch (NumberFormatException e) {
            // Se algo der errado na conversão para int (não deveria acontecer por causa da regex)
            return false;
        }
    }

    // Métodos auxiliares privados para checar os limites
    private boolean isHoraValida(int hora) {
        return hora >= 0 && hora <= 23;
    }

    private boolean isMinutoValido(int minuto) {
        return minuto >= 0 && minuto <= 59;
    }
}