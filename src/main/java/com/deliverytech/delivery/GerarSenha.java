package com.deliverytech.delivery;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Classe utilitária (ferramenta) para geração de hashes de senha BCrypt.
 *
 * Esta classe NÃO é um componente Spring gerenciado e não faz parte da
 * aplicação principal.
 *
 * Seu único propósito é facilitar o desenvolvimento, permitindo gerar
 * hashes de senha (como para "123456") que podem ser copiados e colados
 * diretamente no script de dados de teste (ex: {@code data.sql}).
 *
 * Isso é necessário porque a aplicação armazena senhas usando BCrypt
 * e não em texto plano.
 */
public class GerarSenha {

    /**
     * Método principal (executável) para gerar um hash.
     *
     * Para usar:
     * 1. Defina a senha desejada na variável {@code senha}.
     * 2. Execute este método (clique direito > Run 'GerarSenha.main()').
     * 3. Copie o hash impresso no console.
     * 4. Cole o hash no seu arquivo {@code data.sql} na coluna 'senha'
     * da tabela 'usuario'.
     *
     * @param args Argumentos de linha de comando (não utilizados).
     */
    public static void main(String[] args) {
        // Instancia o mesmo codificador de senha usado pela configuração de segurança
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Define a senha em texto plano que você deseja codificar
        String senha = "123456"; 
        
        // Gera o hash BCrypt
        String hash = encoder.encode(senha);
        
        // Imprime o resultado no console
        System.out.println("====================================================================");
        System.out.println("Senha Plana: " + senha);
        System.out.println("Hash BCrypt: " + hash);
        System.out.println("====================================================================");
    }
}