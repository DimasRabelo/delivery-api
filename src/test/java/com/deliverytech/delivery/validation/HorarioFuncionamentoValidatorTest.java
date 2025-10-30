package com.deliverytech.delivery.validation;

import org.junit.jupiter.api.BeforeEach; // Import BeforeEach
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários do HorarioFuncionamentoValidator")
class HorarioFuncionamentoValidatorTest {

    // Instância do validador que será testado
    private HorarioFuncionamentoValidator validator;

    // Método de setup: cria uma nova instância do validador antes de cada teste
    @BeforeEach
    void setUp() {
        validator = new HorarioFuncionamentoValidator();
        // Chamada initialize explícita (embora vazia, boa prática chamar)
        // O segundo argumento (a anotação) pode ser null se initialize não a usa.
        validator.initialize(null);
    }

    @Test
    @DisplayName("Deve retornar true para horários válidos no formato HH:mm-HH:mm")
    void deveValidarHorariosCorretos() {
        assertTrue(validator.isValid("09:00-18:00", null), "Horário comercial padrão");
        assertTrue(validator.isValid("00:00-23:59", null), "Dia inteiro");
        assertTrue(validator.isValid("23:59-00:00", null), "Virada da noite");
        assertTrue(validator.isValid("12:30-13:30", null), "Horário de almoço");
        assertTrue(validator.isValid("08:15-17:45", null), "Minutos quebrados");
    }

    @Test
    @DisplayName("Deve retornar true para valor null ou vazio")
    void deveConsiderarNullOuVazioValido() {
        // A lógica do validador considera null/vazio como válido,
        // pois @NotBlank/@NotNull cuidam da obrigatoriedade.
        assertTrue(validator.isValid(null, null), "Valor null deve ser considerado válido pelo formatador");
        assertTrue(validator.isValid("", null), "String vazia deve ser considerada válida pelo formatador");
        assertTrue(validator.isValid("   ", null), "String com espaços deve ser considerada válida (após trim na lógica)"); // Ajuste a lógica se não for o caso
    }

    @Test
    @DisplayName("Deve retornar false para formatos incorretos")
    void deveInvalidarFormatosIncorretos() {
        assertFalse(validator.isValid("9:00-18:00", null), "Hora inicial com um dígito");
        assertFalse(validator.isValid("09:0-18:00", null), "Minuto inicial com um dígito");
        assertFalse(validator.isValid("09:00-18:0", null), "Minuto final com um dígito");
        assertFalse(validator.isValid("09:00 - 18:00", null), "Espaço extra");
        assertFalse(validator.isValid("09:00_18:00", null), "Separador incorreto");
        assertFalse(validator.isValid("09:00-18h00", null), "Caracteres extras");
        assertFalse(validator.isValid("09:00-", null), "Falta horário final");
        assertFalse(validator.isValid("-18:00", null), "Falta horário inicial");
        assertFalse(validator.isValid("09:0018:00", null), "Falta separador");
    }

    @Test
    @DisplayName("Deve retornar false para valores fora do range (horas/minutos)")
    void deveInvalidarValoresForaDoRange() {
        assertFalse(validator.isValid("24:00-18:00", null), "Hora inicial inválida (24)");
        assertFalse(validator.isValid("09:60-18:00", null), "Minuto inicial inválido (60)");
        assertFalse(validator.isValid("09:00-25:00", null), "Hora final inválida (25)");
        assertFalse(validator.isValid("09:00-18:61", null), "Minuto final inválido (61)");
        assertFalse(validator.isValid("-1:00-18:00", null), "Hora negativa (não pega pelo regex, mas é bom saber)"); // Regex atual não pega negativo
        assertFalse(validator.isValid("09:00--1:00", null), "Hora negativa (não pega pelo regex, mas é bom saber)"); // Regex atual não pega negativo
    }
}