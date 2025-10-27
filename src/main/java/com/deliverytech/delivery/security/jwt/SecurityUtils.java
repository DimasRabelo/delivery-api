package com.deliverytech.delivery.security.jwt;

import com.deliverytech.delivery.entity.Usuario;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Classe utilitária (Helper) para acessar facilmente os dados do usuário
 * autenticado no sistema.
 *
 * Esta classe centraliza a lógica de busca do usuário no
 * {@link SecurityContextHolder}, simplificando o código em
 * Services e Controllers que precisam saber "quem é o usuário logado".
 *
 * A maioria dos métodos é estática para permitir acesso direto
 * (ex: {@code SecurityUtils.getCurrentUserId()}).
 */
public class SecurityUtils {

    /**
     * Obtém a entidade {@link Usuario} completa do usuário atualmente autenticado.
     *
     * Este é o método central da classe, buscando o 'Principal' no contexto de segurança
     * e fazendo o "cast" para a nossa entidade {@link Usuario}.
     *
     * @return A entidade {@link Usuario} logada.
     * @throws RuntimeException Se não houver usuário autenticado no contexto
     * ou se o 'Principal' não for uma instância de {@link Usuario}.
     */
    public static Usuario getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Verifica se existe autenticação e se o "principal" (usuário)
        // é do tipo que esperamos (UserDetails, que nossa entidade Usuario implementa).
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            // Faz o cast seguro para nossa entidade
            return (Usuario) authentication.getPrincipal();
        }

        // Lança uma exceção se ninguém estiver logado.
        // Isso evita NullPointerExceptions nos métodos que chamam este.
        throw new RuntimeException("Usuário não autenticado");
    }

    /**
     * Atalho para obter o ID do usuário logado.
     *
     * @return O ID (Long) do usuário.
     * @throws RuntimeException Se não houver usuário autenticado.
     */
    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /**
     * Atalho para obter o Email (username) do usuário logado.
     *
     * @return O Email (String) do usuário.
     * @throws RuntimeException Se não houver usuário autenticado.
     */
    public static String getCurrentUserEmail() {
        return getCurrentUser().getEmail();
    }

    /**
     * Atalho para obter a Role (como String) do usuário logado.
     *
     * @return O nome da Role (ex: "CLIENTE", "ADMIN").
     * @throws RuntimeException Se não houver usuário autenticado.
     */
    public static String getCurrentUserRole() {
        return getCurrentUser().getRole().name();
    }

    /**
     * Atalho para obter o ID do restaurante associado ao usuário logado.
     *
     * @return O ID do restaurante (Long), ou 'null' se não houver.
     * @throws RuntimeException Se não houver usuário autenticado.
     */
    public static Long getCurrentRestauranteId() {
        Usuario usuario = getCurrentUser();
        return usuario.getRestauranteId();
    }

    /**
     * Verifica se o usuário logado possui uma role específica.
     *
     * @param role O nome da role a ser verificada (ex: "ADMIN").
     * @return 'true' se o usuário tiver a role, 'false' caso contrário
     * (ou se nenhum usuário estiver logado).
     */
    public static boolean hasRole(String role) {
        try {
            Usuario usuario = getCurrentUser();
            return usuario.getRole().name().equals(role);
        } catch (Exception e) {
            // Se getCurrentUser() lançar exceção (ninguém logado),
            // o usuário com certeza não tem a role.
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // Atalhos de verificação de Role
    // -------------------------------------------------------------------------

    /** @return 'true' se o usuário logado for ADMIN, 'false' caso contrário. */
    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /** @return 'true' se o usuário logado for CLIENTE, 'false' caso contrário. */
    public static boolean isCliente() {
        return hasRole("CLIENTE");
    }

    /** @return 'true' se o usuário logado for RESTAURANTE, 'false' caso contrário. */
    public static boolean isRestaurante() {
        return hasRole("RESTAURANTE");
    }

    /** @return 'true' se o usuário logado for ENTREGADOR, 'false' caso contrário. */
    public static boolean isEntregador() {
        return hasRole("ENTREGADOR");
    }

    // -------------------------------------------------------------------------
    // Métodos de Instância (para Injeção de Dependência)
    // -------------------------------------------------------------------------

    /**
     * Método não-estático que atua como um wrapper para {@link #getCurrentUser()}.
     *
     * Este método provavelmente existe para permitir que a classe {@code SecurityUtils}
     * seja injetada como um Bean Spring (ex: no {@code ProdutoServiceImpl})
     * em vez de chamar o método estático diretamente.
     *
     * @return A entidade {@link Usuario} logada.
     * @throws RuntimeException Se não houver usuário autenticado.
     */
    public Usuario getUsuarioLogado() {
        return getCurrentUser(); // Apenas chama o método estático
    }
}