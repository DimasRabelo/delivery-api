package com.deliverytech.delivery.security.jwt;

//import com.deliverytech.delivery.entity.Restaurante;
import com.deliverytech.delivery.entity.Usuario;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails; 

/**
 * Classe utilitária (Helper) para acessar facilmente os dados do usuário
 * autenticado no sistema.
 */
public final class SecurityUtils {

    private static final String USUARIO_NAO_AUTENTICADO = "Usuário não autenticado";

    public static Usuario getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || 
            !authentication.isAuthenticated() || 
            authentication.getPrincipal() == null) {
            
            throw new RuntimeException(USUARIO_NAO_AUTENTICADO); 
        }

        Object principal = authentication.getPrincipal();
        
        if (!(principal instanceof Usuario)) {
            throw new RuntimeException(USUARIO_NAO_AUTENTICADO); 
        }

        return (Usuario) principal;
    }

    // --- MÉTODOS AUXILIARES ---

    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public static Long getCurrentRestauranteId() {
        Usuario usuario = getCurrentUser();
        
        if (usuario.getRestaurante() != null) {
            return usuario.getRestaurante().getId();
        }
        
        return null;
    }
    
    public static String getCurrentUserEmail() {
        return getCurrentUser().getEmail();
    }

    public static String getCurrentUserRole() {
        return getCurrentUser().getRole().name();
    }

    /**
     * Verifica se o usuário logado possui uma Role específica.
     */
    public static boolean hasRole(String role) {
        try {
            Usuario usuario = getCurrentUser();
            return usuario.getRole().name().equals(role);
        } catch (RuntimeException e) {
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // MÉTODOS DE INSTÂNCIA QUE ESTAVAM FALTANDO NO ARQUIVO DE PRODUÇÃO
    // -------------------------------------------------------------------------

    // Métodos de Instância (para Injeção de Dependência)
    public Usuario getUsuarioLogado() {
        return getCurrentUser(); 
    }
    
    // MÉTODOS ESTÁTICOS DE VERIFICAÇÃO DE ROLE (QUE OS TESTES ESTAVAM CHAMANDO)
    // ESTES DEVERIAM SER MÉTODOS ESTÁTICOS NO SEU CÓDIGO DE PRODUÇÃO.
    
    public static boolean isCliente() { return hasRole("CLIENTE"); }
    public static boolean isAdmin() { return hasRole("ADMIN"); }
    public static boolean isRestaurante() { return hasRole("RESTAURANTE"); }
    public static boolean isEntregador() { return hasRole("ENTREGADOR"); }
}