// package com.deliverytech.delivery.service; 

// import com.deliverytech.delivery.dto.request.EnderecoDTO;
// import com.deliverytech.delivery.dto.request.RestauranteDTO;
// import com.deliverytech.delivery.dto.response.RestauranteResponseDTO;
// import com.deliverytech.delivery.entity.Restaurante;
// import com.deliverytech.delivery.entity.Endereco; // IMPORT ADICIONADO
// import com.deliverytech.delivery.exception.ConflictException;
// import com.deliverytech.delivery.exception.EntityNotFoundException;
// import com.deliverytech.delivery.repository.EnderecoRepository; // IMPORT ADICIONADO
// import com.deliverytech.delivery.repository.RestauranteRepository;
// import com.deliverytech.delivery.service.impl.RestauranteServiceImpl;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.mockito.junit.jupiter.MockitoSettings;
// import org.mockito.quality.Strictness;
// import org.modelmapper.ModelMapper;
// import java.math.BigDecimal;
// import java.util.Optional;
// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class)
// @MockitoSettings(strictness = Strictness.LENIENT)
// @DisplayName("Testes do RestauranteService (Refatorado)")
// class RestauranteServiceTest {

//     // --- DEPENDÊNCIAS MOCADAS ---
//     @Mock
//     private RestauranteRepository restauranteRepository;

//     // --- MUDANÇA (GARGALO 1) ---
//     @Mock
//     private EnderecoRepository enderecoRepository; // <-- ADICIONADO (embora não usado diretamente)

//     @Mock
//     private ModelMapper modelMapper;

//     // --- SYSTEM UNDER TEST (SUT) ---
//     @InjectMocks
//     private RestauranteServiceImpl restauranteService;

//     // --- OBJETOS DE APOIO (REFATORADOS) ---
//     private RestauranteDTO restauranteDTO;
//     private EnderecoDTO enderecoDTO; // <-- NOVO
//     private Restaurante restaurante;
//     private Endereco endereco; // <-- NOVO
//     private RestauranteResponseDTO restauranteResponseDTO;
//     private Long restauranteId = 1L;
//     private String restauranteNome = "Pizzaria Boa";

//     @BeforeEach
//     void setUp() {
        
//         // 1. DTO de Endereço
//         enderecoDTO = new EnderecoDTO();
//         enderecoDTO.setRua("Rua X, 100");
//         enderecoDTO.setCep("01001000");
//         enderecoDTO.setNumero("100");
//         enderecoDTO.setBairro("Centro");
//         enderecoDTO.setCidade("SP");
//         enderecoDTO.setEstado("SP");

//         // 2. DTO de Restaurante (Refatorado)
//         restauranteDTO = new RestauranteDTO();
//         restauranteDTO.setNome(restauranteNome);
//         restauranteDTO.setCategoria("Pizzas");
//         restauranteDTO.setTelefone("11988887777");
//         restauranteDTO.setTaxaEntrega(BigDecimal.valueOf(10.00));
//         restauranteDTO.setAtivo(true);
//         restauranteDTO.setEndereco(enderecoDTO); // <-- CORRIGIDO: Recebe o DTO

//         // 3. Entidade Endereço
//         endereco = new Endereco();
//         endereco.setId(1L);
//         endereco.setRua("Rua X, 100");
//         endereco.setCep("01001000");

//         // 4. Entidade Restaurante (Refatorado)
//         restaurante = new Restaurante();
//         restaurante.setId(restauranteId);
//         restaurante.setNome(restauranteNome);
//         restaurante.setCategoria("Pizzas");
//         restaurante.setTelefone("11988887777");
//         restaurante.setTaxaEntrega(BigDecimal.valueOf(10.00));
//         restaurante.setAtivo(true);
//         restaurante.setEndereco(endereco); // <-- CORRIGIDO: Recebe a Entidade

//         // 5. ResponseDTO (OK)
//         restauranteResponseDTO = new RestauranteResponseDTO();
//         restauranteResponseDTO.setId(restauranteId);
//         restauranteResponseDTO.setNome(restauranteNome);
//         restauranteResponseDTO.setAtivo(true);
//     }

//     // ==========================================================
//     // Testes do cadastrarRestaurante (Refatorado)
//     // ==========================================================

//     @Test
//     @DisplayName("Cadastrar: Deve cadastrar restaurante e endereço com sucesso")
//     void cadastrarRestaurante_DeveRetornarDTO_QuandoSucesso() {
//         // -----------------
//         // Given (Arrange)
//         // -----------------
//         when(restauranteRepository.findByNome(restauranteNome)).thenReturn(Optional.empty());
//         // 1. Simula o ModelMapper convertendo DTO para Entidade Restaurante
//         when(modelMapper.map(restauranteDTO, Restaurante.class)).thenReturn(restaurante);
//         // 2. Simula o ModelMapper convertendo DTO para Entidade Endereco
//         when(modelMapper.map(enderecoDTO, Endereco.class)).thenReturn(endereco);
//         // 3. Simula o 'save' (o Cascade salvará o endereço junto)
//         when(restauranteRepository.save(any(Restaurante.class))).thenReturn(restaurante);
//         // 4. Simula o ModelMapper de resposta
//         when(modelMapper.map(restaurante, RestauranteResponseDTO.class)).thenReturn(restauranteResponseDTO);

//         // -----------------
//         // When (Act)
//         // -----------------
//         RestauranteResponseDTO resultado = restauranteService.cadastrarRestaurante(restauranteDTO);

//         // -----------------
//         // Then (Assert)
//         // -----------------
//         assertNotNull(resultado);
//         assertEquals(restauranteNome, resultado.getNome());
//         assertTrue(restaurante.getAtivo());
        
//         // Verifica se o endereço foi setado na entidade antes de salvar
//         assertEquals(endereco, restaurante.getEndereco());
//         verify(restauranteRepository).save(any(Restaurante.class));
//     }

//     // ... (O teste 'cadastrarRestaurante_DeveLancarExcecao_QuandoNomeJaExiste' 
//     //      continua válido e não precisa de mudanças)

//     @Test
//     @DisplayName("Cadastrar: Deve lançar exceção quando Nome é nulo (validação)")
//     void cadastrarRestaurante_DeveLancarExcecao_QuandoNomeNulo() {
//         // Given
//         restauranteDTO.setNome(null);
//         when(modelMapper.map(restauranteDTO, Restaurante.class)).thenReturn(restaurante);
//         restaurante.setNome(null); // Simula o DTO mapeado

//         // When & Then
//         ConflictException exception = assertThrows(
//                 ConflictException.class,
//                 () -> restauranteService.cadastrarRestaurante(restauranteDTO)
//         );
//         assertEquals("Nome é obrigatório", exception.getMessage());
//         verify(restauranteRepository, never()).save(any());
//     }

//     // ... (O teste 'cadastrarRestaurante_DeveLancarExcecao_QuandoTaxaEntregaNegativa'
//     //      continua válido)

//     // ==========================================================
//     // Testes do atualizarRestaurante (Refatorado)
//     // ==========================================================
    
//     @Test
//     @DisplayName("Atualizar: Deve atualizar restaurante e endereço com sucesso")
//     void atualizarRestaurante_DeveAtualizarComSucesso() {
//         // Given
//         when(restauranteRepository.findById(restauranteId)).thenReturn(Optional.of(restaurante));
        
//         // (O DTO tem os mesmos dados, o service vai mapeá-los para a entidade)
        
//         when(restauranteRepository.save(any(Restaurante.class))).thenReturn(restaurante);
//         when(modelMapper.map(restaurante, RestauranteResponseDTO.class)).thenReturn(restauranteResponseDTO);

//         // When
//         restauranteService.atualizarRestaurante(restauranteId, restauranteDTO);

//         // Then
//         // Verifica se o ModelMapper foi chamado para atualizar o endereço
//         verify(modelMapper).map(enderecoDTO, endereco);
//         // Verifica se o save foi chamado
//         verify(restauranteRepository).save(restaurante);
//     }
    

//     // ==========================================================
//     // (O resto dos seus testes: buscarPorId, alterarStatus, calcularTaxa,
//     //  estão 100% corretos e não são afetados pelo Gargalo 1)
//     // ==========================================================
    
//     @Test
//     @DisplayName("Buscar por ID: Deve lançar exceção quando ID não existe")
//     void buscarRestaurantePorId_DeveLancarExcecao_QuandoIdNaoExiste() {
//         // (Teste OK)
//         when(restauranteRepository.findById(restauranteId)).thenReturn(Optional.empty());
//         EntityNotFoundException exception = assertThrows(
//                 EntityNotFoundException.class,
//                 () -> restauranteService.buscarRestaurantePorId(restauranteId)
//         );
//         assertEquals("Restaurante não encontrado: " + restauranteId, exception.getMessage());
//     }
    
//     @Test
//     @DisplayName("Alterar Status: Deve desativar um restaurante ativo")
//     void alterarStatusRestaurante_DeveDesativarRestauranteAtivo() {
//         // (Teste OK)
//         assertTrue(restaurante.getAtivo()); 
//         when(restauranteRepository.findById(restauranteId)).thenReturn(Optional.of(restaurante));
//         when(restauranteRepository.save(any(Restaurante.class))).thenReturn(restaurante);
//         when(modelMapper.map(restaurante, RestauranteResponseDTO.class)).thenReturn(restauranteResponseDTO);

//         restauranteService.alterarStatusRestaurante(restauranteId);

//         assertFalse(restaurante.getAtivo());
//         verify(restauranteRepository).save(restaurante);
//     }
    
//     @Test
//     @DisplayName("Calcular Taxa: Deve somar 5.00 quando CEP tem final ímpar")
//     void calcularTaxaEntrega_DeveSomarAdicional_QuandoCepFinalImpar() {
//         // (Teste OK)
//         when(restauranteRepository.findById(restauranteId)).thenReturn(Optional.of(restaurante));
//         String cepFinalImpar = "12345-003";
//         BigDecimal taxa = restauranteService.calcularTaxaEntrega(restauranteId, cepFinalImpar);
//         assertEquals(0, BigDecimal.valueOf(15.00).compareTo(taxa));
//     }
// }