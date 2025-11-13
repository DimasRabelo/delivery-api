package com.deliverytech.delivery.security.jwt;

import com.deliverytech.delivery.entity.Usuario;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Classe utilitária (Helper) para acessar facilmente os dados do usuário
 * autenticado no sistema.
 */
public class SecurityUtils {

    /**
     * Obtém a entidade Usuario completa do usuário autenticado.
     */
    public static Usuario getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            return (Usuario) authentication.getPrincipal();
        }
        
        // Retorna null se não estiver logado (evita exceção em alguns casos)
        // Ou mantenha a exceção se preferir forçar o login
        return null; 
    }

    /**
     * Atalho para obter o ID do usuário logado.
     */
    public static Long getCurrentUserId() {
        Usuario user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /**
     * Atalho para obter o Email do usuário logado.
     */
    public static String getCurrentUserEmail() {
        Usuario user = getCurrentUser();
        return user != null ? user.getEmail() : null;
    }

    /**
     * Atalho para obter a Role do usuário logado.
     */
    public static String getCurrentUserRole() {
        Usuario user = getCurrentUser();
        return user != null ? user.getRole().name() : null;
    }

    /**
     * Atalho para obter o ID do restaurante associado ao usuário logado.
     * --- CORREÇÃO APLICADA AQUI ---
     */
    public static Long getCurrentRestauranteId() {
        Usuario usuario = getCurrentUser();
        
        // Verifica se o usuário existe E se ele tem um restaurante vinculado
        if (usuario != null && usuario.getRestaurante() != null) {
            // Navega pelo objeto: Usuario -> Restaurante -> ID
            return usuario.getRestaurante().getId();
        }
        
        return null;
    }

    /**
     * Verifica se o usuário logado possui uma role específica.
     */
    public static boolean hasRole(String role) {
        Usuario usuario = getCurrentUser();
        return usuario != null && usuario.getRole().name().equals(role);
    }

    // -------------------------------------------------------------------------
    // Atalhos de verificação de Role
    // -------------------------------------------------------------------------

    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }

    public static boolean isCliente() {
        return hasRole("CLIENTE");
    }

    public static boolean isRestaurante() {
        return hasRole("RESTAURANTE");
    }

    public static boolean isEntregador() {
        return hasRole("ENTREGADOR");
    }

    // -------------------------------------------------------------------------
    // Métodos de Instância (para Injeção de Dependência)
    // -------------------------------------------------------------------------

    public Usuario getUsuarioLogado() {
        Usuario user = getCurrentUser();
        if (user == null) {
            throw new RuntimeException("Usuário não autenticado");
        }
        return user;
    }
}