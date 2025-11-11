// package com.deliverytech.delivery.validation;

// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import static org.junit.jupiter.api.Assertions.*;

// @DisplayName("Testes Unitários do CpfValidator")
// class CpfValidatorTest {

//     @Test
//     @DisplayName("Deve retornar true para CPFs válidos (com e sem máscara)")
//     void deveValidarCpfsValidos() {
//         // CPFs gerados que você forneceu (assumindo que são válidos)
//         assertTrue(CpfValidator.isValid("611.680.291-55"), "CPF gerado 1");
//         assertTrue(CpfValidator.isValid("773.058.400-09"), "CPF gerado 2");
//         assertTrue(CpfValidator.isValid("076.613.210-25"), "CPF gerado 3");
//         assertTrue(CpfValidator.isValid("945.891.542-26"), "CPF gerado 4");
//         assertTrue(CpfValidator.isValid("316.197.033-00"), "CPF gerado 5");
//         assertTrue(CpfValidator.isValid("636.391.075-78"), "CPF gerado 6");
//         assertTrue(CpfValidator.isValid("884.168.167-51"), "CPF gerado 7");
//         assertTrue(CpfValidator.isValid("430.978.725-82"), "CPF gerado 8");
//         assertTrue(CpfValidator.isValid("029.091.918-50"), "CPF gerado 9");
//         assertTrue(CpfValidator.isValid("978.140.032-37"), "CPF gerado 10");

//         // Exemplos adicionais (um sem máscara)
//         assertTrue(CpfValidator.isValid("11144477735"), "CPF válido conhecido sem máscara");
//     }

//     @Test
//     @DisplayName("Deve retornar false para CPFs inválidos (formato, tamanho, letras)")
//     void deveInvalidarCpfsComFormatoErrado() {
//         assertFalse(CpfValidator.isValid(null), "CPF nulo");
//         assertFalse(CpfValidator.isValid(""), "CPF vazio");
//         assertFalse(CpfValidator.isValid("   "), "CPF com espaços em branco");
//         assertFalse(CpfValidator.isValid("123"), "CPF curto demais");
//         assertFalse(CpfValidator.isValid("123456789012"), "CPF longo demais");
//         assertFalse(CpfValidator.isValid("abcdefghijk"), "CPF com letras");
//         assertFalse(CpfValidator.isValid("123.456.789-AB"), "CPF com letras na máscara");
//     }

//     @Test
//     @DisplayName("Deve retornar false para CPFs com todos os dígitos repetidos")
//     void deveInvalidarCpfsComDigitosRepetidos() {
//         assertFalse(CpfValidator.isValid("00000000000"), "CPF com zeros repetidos");
//         assertFalse(CpfValidator.isValid("11111111111"), "CPF com uns repetidos");
//         assertFalse(CpfValidator.isValid("22222222222"), "CPF com dois repetidos");
//         assertFalse(CpfValidator.isValid("33333333333"), "CPF com três repetidos");
//         assertFalse(CpfValidator.isValid("44444444444"), "CPF com quatros repetidos");
//         assertFalse(CpfValidator.isValid("55555555555"), "CPF com cincos repetidos");
//         assertFalse(CpfValidator.isValid("66666666666"), "CPF com seis repetidos");
//         assertFalse(CpfValidator.isValid("77777777777"), "CPF com setes repetidos");
//         assertFalse(CpfValidator.isValid("88888888888"), "CPF com oitos repetidos");
//         assertFalse(CpfValidator.isValid("99999999999"), "CPF com noves repetidos");
//     }

//     @Test
//     @DisplayName("Deve retornar false para CPFs com dígitos verificadores incorretos")
//     void deveInvalidarCpfsComDigitosVerificadoresErrados() {
//         assertFalse(CpfValidator.isValid("111.444.777-30"), "Primeiro dígito verificador errado");
//         assertFalse(CpfValidator.isValid("52998224720"), "Segundo dígito verificador errado");
//         assertFalse(CpfValidator.isValid("12345678900"), "Ambos dígitos verificadores errados");
//         assertFalse(CpfValidator.isValid("98765432199"), "Outro CPF com dígitos errados");
//     }
// }