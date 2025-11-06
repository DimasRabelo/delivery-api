package com.deliverytech.delivery.controller.auth;

import com.deliverytech.delivery.dto.EnderecoDTO; // IMPORT ADICIONADO
import com.deliverytech.delivery.dto.auth.LoginRequest;
import com.deliverytech.delivery.dto.auth.RegisterRequest; // (Este é o DTO refatorado)
import com.deliverytech.delivery.entity.Cliente; // IMPORT ADICIONADO
import com.deliverytech.delivery.entity.Endereco; // IMPORT ADICIONADO
import com.deliverytech.delivery.entity.Usuario;
import com.deliverytech.delivery.enums.Role;
// --- REPOSITÓRIOS ADICIONADOS ---
import com.deliverytech.delivery.repository.ClienteRepository;
import com.deliverytech.delivery.repository.EnderecoRepository;
import com.deliverytech.delivery.repository.PedidoRepository;
import com.deliverytech.delivery.repository.ProdutoRepository;
import com.deliverytech.delivery.repository.RestauranteRepository;
import com.deliverytech.delivery.repository.auth.UsuarioRepository;
// --- FIM DOS REPOSITÓRIOS ---
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

//import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Testes de Integração do AuthController (Refatorado)")
class AuthControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    // --- DEPENDÊNCIAS ADICIONADAS PARA SETUP ---
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private EnderecoRepository enderecoRepository;
    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private RestauranteRepository restauranteRepository;

    @BeforeEach
    void setup() {
        // Limpa o banco na ordem correta para evitar erros de constraint
        pedidoRepository.deleteAll();
        produtoRepository.deleteAll();
        restauranteRepository.deleteAll();
        // (O cascade deve limpar Cliente e Enderecos do cliente)
        usuarioRepository.deleteAll();
        // (Limpa explicitamente Endereco e Cliente caso cascade não funcione)
        enderecoRepository.deleteAll();
        clienteRepository.deleteAll();
    }

    /**
     * Método auxiliar (REFATORADO) para criar um usuário COMPLETO no banco
     * (Usuario + Cliente + Endereco)
     */
    private Usuario criarClienteCompletoNoBanco(String nome, String email, String senha) {
        // 1. Cria Usuario (Autenticação)
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(senha)); // Criptografa
        usuario.setRole(Role.CLIENTE);
        usuario.setAtivo(true);
        // (usuario.setNome() foi removido - CORRIGE O ERRO)

        // 2. Cria Cliente (Perfil)
        Cliente cliente = new Cliente();
        cliente.setNome(nome);
        cliente.setCpf("11122233344");
        cliente.setTelefone("11988887777");

        // 3. Cria Endereço
        Endereco endereco = new Endereco();
        endereco.setApelido("Casa Teste");
        endereco.setCep("01001000");
        endereco.setRua("Rua Teste");
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

    /**
     * Helper para criar o DTO de Endereço (necessário para o registro)
     */
    private EnderecoDTO criarEnderecoDTO() {
        EnderecoDTO endereco = new EnderecoDTO();
        endereco.setCep("01001000");
        endereco.setRua("Rua Teste DTO");
        endereco.setNumero("123");
        endereco.setBairro("Bairro DTO");
        endereco.setCidade("Cidade DTO");
        endereco.setEstado("SP");
        return endereco;
    }

    // ==========================================================
    // Testes do /login (Corrigido)
    // ==========================================================

    @Test
    @DisplayName("Login: Deve retornar 401 (Unauthorized) quando a senha está errada")
    void login_ShouldReturn401_WhenPasswordIsWrong() throws Exception {
        // Given
        // 1. Cria um usuário COMPLETO no banco
        criarClienteCompletoNoBanco("User Teste", "user@email.com", "senha-certa");

        // 2. Prepara uma requisição de login com a SENHA ERRADA
        LoginRequest loginRequest = new LoginRequest("user@email.com", "senha-errada");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Credenciais inválidas"));
    }
    
    // (Adicione um teste de login com SUCESSO)
    @Test
    @DisplayName("Login: Deve retornar 200 e Token quando as credenciais estão corretas")
    void login_ShouldReturn200_WhenCredentialsAreCorrect() throws Exception {
        // Given
        criarClienteCompletoNoBanco("User Teste", "user@email.com", "senha-certa");
        LoginRequest loginRequest = new LoginRequest("user@email.com", "senha-certa");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.usuario.email").value("user@email.com"))
                .andExpect(jsonPath("$.usuario.nome").value("User Teste")); // (Valida o DTO de resposta)
    }

    // ==========================================================
    // Testes do /register (Corrigido)
    // ==========================================================
    
    @Test
    @DisplayName("Register: Deve retornar 201 (Created) ao registrar cliente com sucesso")
    void register_ShouldReturn201_WhenRegistrationIsSuccessful() throws Exception {
        // Given
        // 1. Prepara o NOVO RegisterRequest (com EnderecoDTO)
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setNome("Novo Cliente");
        registerRequest.setCpf("12345678901"); // (CPF Válido)
        registerRequest.setTelefone("11999998888");
        registerRequest.setEmail("novo.cliente@email.com");
        registerRequest.setSenha("senha123");
        registerRequest.setEndereco(criarEnderecoDTO()); // <-- Adiciona o Endereco

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                // 1. O AuthService refatorado deve funcionar
                .andExpect(status().isCreated())
                // 2. Valida a resposta (UserResponse)
                .andExpect(jsonPath("$.email").value("novo.cliente@email.com"))
                .andExpect(jsonPath("$.nome").value("Novo Cliente")) // (O UserResponse sabe pegar o nome do Cliente)
                .andExpect(jsonPath("$.role").value("CLIENTE"));
    }

    @Test
    @DisplayName("Register: Deve retornar 400 (Bad Request) quando email já existe (Refatorado)")
    void register_ShouldReturn400_WhenEmailAlreadyExists() throws Exception {
        // Given
        // 1. Cria um usuário COMPLETO no banco
        criarClienteCompletoNoBanco("Cliente Antigo", "email.existente@email.com", "senha123");

        // 2. Prepara uma requisição de registro com o MESMO email
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setNome("Outro Nome");
        registerRequest.setCpf("98765432100");
        registerRequest.setTelefone("11988887777");
        registerRequest.setEmail("email.existente@email.com"); // <-- Email duplicado
        registerRequest.setSenha("outraSenha");
        registerRequest.setEndereco(criarEnderecoDTO());
        // (O campo 'setRole' foi removido - CORRIGE O ERRO)

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email já está em uso"));
    }

    // ==========================================================
    // Testes do /me (Seu teste original - Está OK)
    // ==========================================================
    
    @Test
    @WithMockUser // Esta anotação NÃO cria um 'Usuario', cria um 'MockUser'
    @DisplayName("GetMe: Deve retornar 401 se o Principal não for 'Usuario' (ex: @WithMockUser)")
    void getMe_ShouldReturn401_WhenPrincipalIsNotUsuarioInstance() throws Exception {
        // Given: @WithMockUser
        // When & Then
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Token inválido. Acesso negado."));
    }
}