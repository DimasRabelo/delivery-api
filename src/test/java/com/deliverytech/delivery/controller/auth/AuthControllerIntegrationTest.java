package com.deliverytech.delivery.controller.auth;

import com.deliverytech.delivery.dto.EnderecoDTO;
import com.deliverytech.delivery.dto.auth.LoginRequest;
import com.deliverytech.delivery.dto.auth.RegisterRequest;
import com.deliverytech.delivery.entity.Cliente;
import com.deliverytech.delivery.entity.Endereco;
import com.deliverytech.delivery.entity.Usuario;
import com.deliverytech.delivery.enums.Role;
import com.deliverytech.delivery.repository.ClienteRepository;
import com.deliverytech.delivery.repository.EnderecoRepository;
import com.deliverytech.delivery.repository.PedidoRepository;
import com.deliverytech.delivery.repository.ProdutoRepository;
import com.deliverytech.delivery.repository.RestauranteRepository;
import com.deliverytech.delivery.repository.auth.UsuarioRepository;
import com.deliverytech.delivery.validation.CpfValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
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

import java.util.Set;

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

    @Autowired private ClienteRepository clienteRepository;
    @Autowired private EnderecoRepository enderecoRepository;
    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private RestauranteRepository restauranteRepository;

    @BeforeEach
    void setup() {
        pedidoRepository.deleteAll();
        produtoRepository.deleteAll();
        restauranteRepository.deleteAll();
        usuarioRepository.deleteAll();
        enderecoRepository.deleteAll();
        clienteRepository.deleteAll();
    }

    private Usuario criarClienteCompletoNoBanco(String nome, String email, String senha, String cpf) {
        if (!CpfValidator.isValid(cpf)) {
            throw new IllegalArgumentException("CPF inválido nos testes: " + cpf);
        }

        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(senha));
        usuario.setRole(Role.CLIENTE);
        usuario.setAtivo(true);

        Cliente cliente = new Cliente();
        cliente.setNome(nome);
        cliente.setCpf(cpf);
        cliente.setTelefone("11988887777");

        Endereco endereco = new Endereco();
        endereco.setApelido("Casa Teste");
        endereco.setCep("01001000");
        endereco.setRua("Rua Teste");
        endereco.setNumero("123");
        endereco.setBairro("Bairro Teste");
        endereco.setCidade("Cidade Teste");
        endereco.setEstado("SP");

        cliente.setUsuario(usuario);
        endereco.setUsuario(usuario);

        usuario.setCliente(cliente);
        usuario.getEnderecos().add(endereco);

        return usuarioRepository.save(usuario);
    }

    private EnderecoDTO criarEnderecoDTO() {
        EnderecoDTO endereco = new EnderecoDTO();
        endereco.setApelido("Casa DTO");
        endereco.setCep("01001000");
        endereco.setRua("Rua Teste DTO");
        endereco.setNumero("123");
        endereco.setBairro("Bairro DTO");
        endereco.setCidade("Cidade DTO");
        endereco.setEstado("SP");
        return endereco;
    }

    // ==========================================================
    // Testes de Login
    // ==========================================================
    @Test
    @DisplayName("Login: Deve retornar 401 (Unauthorized) quando a senha está errada")
    void login_ShouldReturn401_WhenPasswordIsWrong() throws Exception {
        criarClienteCompletoNoBanco("User Teste", "user@email.com", "senha-certa", "52998224725");
        LoginRequest loginRequest = new LoginRequest("user@email.com", "senha-errada");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Credenciais inválidas"));
    }

    @Test
    @DisplayName("Login: Deve retornar 200 e Token quando as credenciais estão corretas")
    void login_ShouldReturn200_WhenCredentialsAreCorrect() throws Exception {
        criarClienteCompletoNoBanco("User Teste", "user@email.com", "senha-certa", "52998224725");
        LoginRequest loginRequest = new LoginRequest("user@email.com", "senha-certa");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.usuario.email").value("user@email.com"))
                .andExpect(jsonPath("$.usuario.nome").value("User Teste"));
    }

    // ==========================================================
    // Testes do /register
    // ==========================================================
    @Test
    @DisplayName("Register: Deve retornar 201 (Created) ao registrar cliente com sucesso")
    void register_ShouldReturn201_WhenRegistrationIsSuccessful() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setNome("Novo Cliente");
        registerRequest.setCpf("52998224725");
        registerRequest.setTelefone("11999998888");
        registerRequest.setEmail("novo.cliente@email.com");
        registerRequest.setSenha("senha123");
        registerRequest.setEndereco(criarEnderecoDTO());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("novo.cliente@email.com"))
                .andExpect(jsonPath("$.nome").value("Novo Cliente"))
                .andExpect(jsonPath("$.role").value("CLIENTE"));
    }

    @Test
    @DisplayName("Register: Deve retornar 400 (Bad Request) quando email já existe")
    void register_ShouldReturn400_WhenEmailAlreadyExists() throws Exception {
        criarClienteCompletoNoBanco("Cliente Antigo", "email.existente@email.com", "senha123", "52998224725");

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setNome("Outro Nome");
        registerRequest.setCpf("82647833079");
        registerRequest.setTelefone("11988887777");
        registerRequest.setEmail("email.existente@email.com");
        registerRequest.setSenha("outraSenha");
        registerRequest.setEndereco(criarEnderecoDTO());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email já está em uso"));
    }

    // ==========================================================
    // Teste /me
    // ==========================================================
    @Test
    @WithMockUser
    @DisplayName("GetMe: Deve retornar 401 se o Principal não for 'Usuario'")
    void getMe_ShouldReturn401_WhenPrincipalIsNotUsuarioInstance() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Token ausente ou inválido. Acesso negado."));
    }

    // ==========================================================
    // Teste auxiliar de validação de RegisterRequest
    // ==========================================================
    @Test
    @DisplayName("Debug RegisterRequest: validar campos antes de enviar")
    void debugRegisterRequestValidation() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setNome("Novo Cliente");
        registerRequest.setCpf("52998224725");
        registerRequest.setTelefone("11999998888");
        registerRequest.setEmail("novo.cliente@email.com");
        registerRequest.setSenha("senha123");

        registerRequest.setEndereco(criarEnderecoDTO());

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(registerRequest);

        if (violations.isEmpty()) {
            System.out.println("✅ Todos os campos válidos!");
        } else {
            System.out.println("❌ Campos com erro de validação:");
            for (ConstraintViolation<RegisterRequest> v : violations) {
                System.out.println("Campo: " + v.getPropertyPath() + " | Valor: " + v.getInvalidValue() + " | Mensagem: " + v.getMessage());
            }
        }
    }
}
