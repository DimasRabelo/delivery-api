package com.deliverytech.delivery.controller;

import com.deliverytech.delivery.config.TestDataConfiguration;
import com.deliverytech.delivery.dto.ClienteDTO; // (Este é o DTO refatorado (só nome, cpf, tel))
import com.deliverytech.delivery.entity.Cliente;
import com.deliverytech.delivery.entity.Endereco; // IMPORT ADICIONADO
import com.deliverytech.delivery.entity.Usuario; // IMPORT ADICIONADO
import com.deliverytech.delivery.enums.Role; // IMPORT ADICIONADO
import com.deliverytech.delivery.repository.ClienteRepository;
// --- NOVOS REPOSITÓRIOS ---
import com.deliverytech.delivery.repository.EnderecoRepository;
import com.deliverytech.delivery.repository.PedidoRepository;
import com.deliverytech.delivery.repository.ProdutoRepository;
import com.deliverytech.delivery.repository.RestauranteRepository;
import com.deliverytech.delivery.repository.auth.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder; // IMPORT ADICIONADO
import org.springframework.security.test.context.support.WithMockUser;
// IMPORT ADICIONADO (Requer um UserDetailsService funcional, como o AuthService)
import org.springframework.security.test.context.support.WithUserDetails; 
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

// --- IMPORTS ESTÁTICOS (CORRIGINDO OS ERROS DE 'post', 'status', 'jsonPath') ---
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
// --- FIM DOS IMPORTS ESTÁTICOS ---


// (O @WithMockUser aqui é um 'admin' genérico para os testes de 'listar' e '404')
@WithMockUser(username = "admin", roles = {"ADMIN"}) 
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestDataConfiguration.class) // (Atenção: TestDataConfiguration precisa estar refatorado)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Testes de Integração do ClienteController (Refatorado)")
class ClienteControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    
    // --- TODOS OS REPOSITÓRIOS NECESSÁRIOS PARA O SETUP ---
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private EnderecoRepository enderecoRepository;
    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private RestauranteRepository restauranteRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        // Limpa o banco na ordem correta
        pedidoRepository.deleteAll();
        produtoRepository.deleteAll();
        restauranteRepository.deleteAll();
        usuarioRepository.deleteAll(); // (O cascade deve limpar Cliente e Enderecos)
        enderecoRepository.deleteAll(); // (Limpa endereços de restaurante)
        clienteRepository.deleteAll();
    }

    /**
     * Método auxiliar (REFATORADO) para criar um usuário COMPLETO no banco
     * (Usuario + Cliente + Endereco)
     */
    private Usuario criarClienteCompletoNoBanco(String nome, String email, String cpf, boolean ativo) {
        // 1. Cria Usuario (Autenticação)
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode("123456"));
        usuario.setRole(Role.CLIENTE);
        usuario.setAtivo(ativo); // <-- CORRIGIDO: 'ativo' é setado no Usuario

        // 2. Cria Cliente (Perfil)
        Cliente cliente = new Cliente();
        cliente.setNome(nome);
        cliente.setCpf(cpf);
        cliente.setTelefone("11988887777");
        // (setEmail e setAtivo removidos daqui - CORRIGE O ERRO)

        // 3. Cria Endereço
        Endereco endereco = new Endereco();
        endereco.setApelido("Casa Teste");
        endereco.setCep("01001000");
        endereco.setRua("Rua Teste"); // <-- CORRIGIDO: 'endereco' é um objeto
        endereco.setNumero("123");
        endereco.setBairro("Bairro Teste");
        endereco.setCidade("Cidade Teste");
        endereco.setEstado("SP");

        // 4. Conecta todos
        cliente.setUsuario(usuario);
        endereco.setUsuario(usuario);
        
        usuario.setCliente(cliente);
        usuario.getEnderecos().add(endereco);
        
        // 5. Salva o Usuário (Cascade salva Cliente e Endereco)
        return usuarioRepository.save(usuario);
    }


    // ------------------------------------------------------------------------
    @Test
    @DisplayName("[POST /api/clientes] - REMOVIDO (Movido para AuthController)")
    void should_CreateCliente_When_ValidData() {
        // O teste de POST /api/clientes foi removido porque
        // o cadastro de cliente agora é feito pelo AuthController (POST /api/auth/register)
        // (Já testamos isso no AuthControllerIntegrationTest.java)
    }

    // ------------------------------------------------------------------------
    @Test
    @DisplayName("[GET /api/clientes/{id}] - Deve buscar cliente por ID")
    void should_ReturnCliente_When_IdExists() throws Exception {
        // Given
        // (Usa o helper refatorado para criar o dado de teste)
        Usuario usuarioSalvo = criarClienteCompletoNoBanco("João Teste", "joao@email.com", "12345678900", true);

        // When & Then
        mockMvc.perform(get("/api/clientes/{id}", usuarioSalvo.getId())) // Usa o ID do Usuário (que é o mesmo do Cliente)
                .andExpect(status().isOk()) 
                .andExpect(jsonPath("$.success", is(true)))
                // (Valida o ClienteResponseDTO refatorado)
                .andExpect(jsonPath("$.data.id", is(usuarioSalvo.getId().intValue())))
                .andExpect(jsonPath("$.data.nome", is("João Teste"))) // (Dado do Cliente)
                .andExpect(jsonPath("$.data.email", is("joao@email.com"))) // (Dado do Usuario)
                .andExpect(jsonPath("$.data.ativo", is(true))); // (Dado do Usuario)
    }

    // ------------------------------------------------------------------------
    @Test
    @DisplayName("[GET /api/clientes/{id}] - Deve retornar 404 quando ID não existe")
    void should_ReturnNotFound_When_ClienteNotExists() throws Exception {
        // (Este teste estava OK)
        mockMvc.perform(get("/api/clientes/{id}", 999L)) 
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Cliente não encontrado")));
    }

    // ------------------------------------------------------------------------
    @Test
    @DisplayName("[GET /api/clientes] - Deve listar clientes ativos (Paginado)")
    void should_ReturnListOfClientes_When_ListarClientesAtivos() throws Exception {
        // Given
        criarClienteCompletoNoBanco("Cliente Ativo", "ativo@email.com", "55544433322", true);
        criarClienteCompletoNoBanco("Cliente Inativo", "inativo@email.com", "11122233344", false);

        // When & Then
        mockMvc.perform(get("/api/clientes")
                .param("page", "0")
                .param("size", "5"))
                
                .andExpect(status().isOk())
                // (O ClienteServiceImpl refatorado só retorna clientes ATIVOS)
                .andExpect(jsonPath("$.content", hasSize(1))) 
                .andExpect(jsonPath("$.content[0].nome", is("Cliente Ativo")))
                .andExpect(jsonPath("$.page.totalElements", is(1)));
    }

    // ------------------------------------------------------------------------
   @Test
   // (Simula que o usuário 'joao.teste@email.com' está logado)
   // (Requer um UserDetailsService (AuthService) que funcione para carregar este usuário)
   @WithUserDetails("joao.teste@email.com") 
   @DisplayName("[PUT /api/clientes/{id}] - Deve atualizar o próprio perfil")
    void should_UpdateCliente_When_ClienteExists() throws Exception {
        // Given
        Usuario usuarioSalvo = criarClienteCompletoNoBanco("Cliente Original", "joao.teste@email.com", "39053344705", true);

        // O DTO DE ATUALIZAÇÃO (REFATORADO)
        // (Não envia email, não envia endereço String)
        ClienteDTO dto = new ClienteDTO();
        dto.setNome("Cliente Atualizado");
        dto.setCpf("39053344705"); // CPF igual (ou outro válido)
        dto.setTelefone("22222222222"); // Telefone mudou

        // When & Then
        mockMvc.perform(put("/api/clientes/{id}", usuarioSalvo.getId()) // Atualiza o próprio ID
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.nome", is("Cliente Atualizado")))
            .andExpect(jsonPath("$.data.telefone", is("22222222222")));
    }

    // ------------------------------------------------------------------------
    @Test
    // (Precisa ser ADMIN para desativar outra conta)
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("[DELETE /api/clientes/{id}] - Deve desativar o usuário (exclusão lógica)")
    void should_DeactivateCliente_When_ClienteExists() throws Exception {
        // Given
        Usuario usuarioSalvo = criarClienteCompletoNoBanco("Cliente para Desativar", "delete@email.com", "20202020200", true);
        assertTrue(usuarioSalvo.getAtivo()); // Garante que começou ativo

        // When
        mockMvc.perform(delete("/api/clientes/{id}", usuarioSalvo.getId()))
                .andExpect(status().isNoContent()); 

        // Then
        // (Verifica se o USUÁRIO foi desativado, não o cliente)
        Usuario usuarioDesativado = usuarioRepository.findById(usuarioSalvo.getId()).orElseThrow();
        assertFalse(usuarioDesativado.getAtivo());
    }
}