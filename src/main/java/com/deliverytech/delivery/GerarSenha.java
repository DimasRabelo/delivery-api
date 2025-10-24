package com.deliverytech.delivery;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GerarSenha {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String senha = "123456"; // senha que você quer gerar o hash
        String hash = encoder.encode(senha);
        System.out.println("Hash gerado: " + hash);
    }
}