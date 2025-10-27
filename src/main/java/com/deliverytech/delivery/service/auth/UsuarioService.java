package com.deliverytech.delivery.service.auth;

import com.deliverytech.delivery.entity.Usuario;
import com.deliverytech.delivery.repository.auth.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Serviço de negócios para operações de Gerenciamento (CRUD) da entidade {@link Usuario}.
 *
 * Este serviço é consumido principalmente pelo
 * {@link com.deliverytech.delivery.controller.auth.UsuarioController} e lida com
 * a lógica de buscar, salvar, atualizar e deletar usuários.
 *
 * @implNote A lógica de *criação* de novos usuários com criptografia de senha
 * está centralizada no {@link AuthService#criarUsuario(com.deliverytech.delivery.dto.auth.RegisterRequest)}.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Construtor para injeção de dependência do repositório de usuário.
     *
     * @param usuarioRepository O repositório para acesso aos dados do usuário.
     */
    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Busca um usuário pelo seu ID (Chave Primária).
     *
     * @param id O ID do usuário a ser buscado.
     * @return A entidade {@link Usuario} correspondente.
     * @throws RuntimeException (ou idealmente, uma exceção customizada) se o
     * usuário não for encontrado com o ID especificado.
     */
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com id: " + id));
    }

    /**
     * Retorna uma lista de todos os usuários cadastrados no sistema.
     *
     * @return Uma {@link List} de entidades {@link Usuario}.
     */
    public List<Usuario> buscarTodos() {
        return usuarioRepository.findAll();
    }

    /**
     * Salva ou atualiza uma entidade {@link Usuario} no banco de dados.
     *
     * @param usuario A entidade {@link Usuario} a ser salva.
     * @return A entidade {@link Usuario} salva (com o ID preenchido, se for nova).
     * @implNote Este método não criptografa a senha. Para criar novos usuários
     * de forma segura, prefira usar {@link AuthService#criarUsuario}.
     */
    public Usuario salvar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    /**
     * Atualiza os dados de um usuário existente.
     *
     * @param id                O ID do usuário a ser atualizado.
     * @param usuarioAtualizado A entidade com os novos dados (vinda do request).
     * @return A entidade {@link Usuario} atualizada e salva no banco.
     * @throws RuntimeException Se o usuário com o 'id' fornecido não for encontrado.
     *
     * @implNote 
     
     * o {@link org.springframework.security.crypto.password.PasswordEncoder}
     * para criptografar a nova senha.
     */
    public Usuario atualizar(Long id, Usuario usuarioAtualizado) {
        // 1. Busca o usuário existente no banco
        Usuario usuario = buscarPorId(id);
        
        // 2. Copia os campos do objeto 'usuarioAtualizado' (vindo do request)
        //    para o objeto 'usuario' (vindo do banco)
        usuario.setNome(usuarioAtualizado.getNome());
        usuario.setEmail(usuarioAtualizado.getEmail());
        usuario.setSenha(usuarioAtualizado.getSenha());
        
        usuario.setRole(usuarioAtualizado.getRole());
        usuario.setAtivo(usuarioAtualizado.getAtivo());
        usuario.setRestauranteId(usuarioAtualizado.getRestauranteId());
        
        // 3. Salva o objeto 'usuario' (agora modificado) de volta no banco
        return usuarioRepository.save(usuario);
    }

    /**
     * Deleta um usuário do banco de dados pelo seu ID.
     *
     * @param id O ID do usuário a ser deletado.
     * @throws RuntimeException Se o usuário com o 'id' fornecido não for encontrado.
     * @implNote Considere usar uma "deleção lógica" (setar {@code usuario.setAtivo(false)})
     * em vez de uma deleção física ({@code delete}) para manter a
     * integridade referencial de registros históricos (ex: pedidos antigos).
     */
    public void deletar(Long id) {
        Usuario usuario = buscarPorId(id);
        usuarioRepository.delete(usuario);
    }
}