package com.deliverytech.delivery.validation;

import java.util.InputMismatchException; // Pode ser usado se preferir lançar exceção em vez de retornar false

public class CpfValidator {

    // Método principal para validar o CPF
    public static boolean isValid(String cpf) {
        // 1. Tratamento inicial: null, vazio ou formato inválido
        if (cpf == null || cpf.isEmpty()) {
            return false;
        }

        // Remove caracteres não numéricos (pontos, traços)
        String cpfLimpo = cpf.replaceAll("\\D", "");

        // 2. Verifica se tem 11 dígitos
        if (cpfLimpo.length() != 11) {
            return false;
        }

        // 3. Verifica se todos os dígitos são iguais (ex: 111.111.111-11), que são inválidos
        if (cpfLimpo.matches("(\\d)\\1{10}")) {
            return false;
        }

        try {
            // 4. Calcula o primeiro dígito verificador
            char dig10 = calcularDigitoVerificador(cpfLimpo.substring(0, 9));

            // 5. Calcula o segundo dígito verificador
            char dig11 = calcularDigitoVerificador(cpfLimpo.substring(0, 9) + dig10);

            // 6. Verifica se os dígitos calculados batem com os dígitos do CPF informado
            return (dig10 == cpfLimpo.charAt(9)) && (dig11 == cpfLimpo.charAt(10));

        } catch (InputMismatchException erro) {
            // Se ocorrer algum erro na conversão (não deveria, pois já limpamos), considera inválido
            return false;
        }
    }

    // Método auxiliar para calcular UM dígito verificador (usado duas vezes)
    private static char calcularDigitoVerificador(String baseCpf) {
        int sm = 0;
        int peso = baseCpf.length() + 1; // Peso começa em 10 para o 1º dígito, 11 para o 2º

        // Multiplica os dígitos pela sequência de pesos decrescente
        for (int i = 0; i < baseCpf.length(); i++) {
            // Converte o i-ésimo caractere em número e multiplica pelo peso
            int num = (int) (baseCpf.charAt(i) - 48); // Subtrai 48 para converter char '0'-'9' para int 0-9
            sm = sm + (num * peso);
            peso = peso - 1;
        }

        // Calcula o resto da divisão por 11
        int r = 11 - (sm % 11);

        char digVerificador;
        // Se o resto for 10 ou 11 (ou 0 ou 1 na lógica do mod 11), o dígito é '0'
        if ((r == 10) || (r == 11)) {
            digVerificador = '0';
        } else {
            // Senão, o dígito é o próprio resto convertido para char
            digVerificador = (char) (r + 48); // Adiciona 48 para converter int 0-9 para char '0'-'9'
        }
        return digVerificador;
    }

    // --- (Opcional) Método main para testes rápidos ---
    /*
    public static void main(String[] args) {
        // Exemplos de CPFs (use geradores online para obter CPFs válidos para teste)
        String cpfValido1 = "111.444.777-35"; // Exemplo válido
        String cpfValido2 = "52998224725";   // Exemplo válido sem máscara
        String cpfInvalido1 = "111.111.111-11"; // Dígitos repetidos
        String cpfInvalido2 = "123.456.789-00"; // Dígitos incorretos
        String cpfInvalido3 = "123";           // Curto demais

        System.out.println(cpfValido1 + ": " + isValid(cpfValido1)); // true
        System.out.println(cpfValido2 + ": " + isValid(cpfValido2)); // true
        System.out.println(cpfInvalido1 + ": " + isValid(cpfInvalido1)); // false
        System.out.println(cpfInvalido2 + ": " + isValid(cpfInvalido2)); // false
        System.out.println(cpfInvalido3 + ": " + isValid(cpfInvalido3)); // false
    }
    */
}