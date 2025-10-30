package com.deliverytech.delivery.service; // Coloque no mesmo pacote dos seus outros testes

import com.deliverytech.delivery.dto.RestauranteDTO;
import com.deliverytech.delivery.dto.response.RestauranteResponseDTO;
import com.deliverytech.delivery.entity.Restaurante;
import com.deliverytech.delivery.exception.ConflictException;
import com.deliverytech.delivery.exception.EntityNotFoundException;
import com.deliverytech.delivery.repository.RestauranteRepository;
import com.deliverytech.delivery.service.impl.RestauranteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do RestauranteService (Impl)")
class RestauranteServiceTest {

    // --- DEPENDÊNCIAS MOCADAS ---
    @Mock
    private RestauranteRepository restauranteRepository;

    @Mock
    private ModelMapper modelMapper;

    // --- SYSTEM UNDER TEST (SUT) ---
    @InjectMocks
    private RestauranteServiceImpl restauranteService;

    // --- OBJETOS DE APOIO PARA OS TESTES ---
    private RestauranteDTO restauranteDTO;
    private Restaurante restaurante;
    private RestauranteResponseDTO restauranteResponseDTO;
    private Long restauranteId = 1L;
    private String restauranteNome = "Pizzaria Boa";

    @BeforeEach
    void setUp() {
        // 1. DTO (Entrada)
        restauranteDTO = new RestauranteDTO();
        restauranteDTO.setNome(restauranteNome);
        restauranteDTO.setCategoria("Pizzas");
        restauranteDTO.setEndereco("Rua X, 100");
        restauranteDTO.setTelefone("11988887777");
        restauranteDTO.setTaxaEntrega(BigDecimal.valueOf(10.00));
        restauranteDTO.setAtivo(true);

        // 2. Entidade (Banco)
        restaurante = new Restaurante();
        restaurante.setId(restauranteId);
        restaurante.setNome(restauranteNome);
        restaurante.setCategoria("Pizzas");
        restaurante.setEndereco("Rua X, 100");
        restaurante.setTelefone("11988887777");
        restaurante.setTaxaEntrega(BigDecimal.valueOf(10.00));
        restaurante.setAtivo(true);

        // 3. ResponseDTO (Saída)
        restauranteResponseDTO = new RestauranteResponseDTO();
        restauranteResponseDTO.setId(restauranteId);
        restauranteResponseDTO.setNome(restauranteNome);
        restauranteResponseDTO.setAtivo(true);
    }

    // ==========================================================
    // Testes do cadastrarRestaurante
    // ==========================================================

    @Test
    @DisplayName("Cadastrar: Deve cadastrar restaurante com sucesso")
    void cadastrarRestaurante_DeveRetornarDTO_QuandoSucesso() {
        // -----------------
        // Given (Arrange)
        // -----------------
        // 1. Simula a verificação de nome (não existe)
        when(restauranteRepository.findByNome(restauranteNome)).thenReturn(Optional.empty());
        // 2. Simula o ModelMapper convertendo DTO para Entidade
        when(modelMapper.map(restauranteDTO, Restaurante.class)).thenReturn(restaurante);
        // 3. Simula o 'save'
        when(restauranteRepository.save(any(Restaurante.class))).thenReturn(restaurante);
        // 4. Simula o ModelMapper convertendo Entidade para ResponseDTO
        when(modelMapper.map(restaurante, RestauranteResponseDTO.class)).thenReturn(restauranteResponseDTO);

        // -----------------
        // When (Act)
        // -----------------
        RestauranteResponseDTO resultado = restauranteService.cadastrarRestaurante(restauranteDTO);

        // -----------------
        // Then (Assert)
        // -----------------
        assertNotNull(resultado);
        assertEquals(restauranteNome, resultado.getNome());
        // Verifica se o service forçou o 'ativo' para true
        assertTrue(restaurante.getAtivo());
        verify(restauranteRepository).save(any(Restaurante.class));
    }

    @Test
    @DisplayName("Cadastrar: Deve lançar exceção quando Nome já existe (Conflito)")
    void cadastrarRestaurante_DeveLancarExcecao_QuandoNomeJaExiste() {
        // Given
        // Simula que o restaurante com este nome JÁ EXISTE
        when(restauranteRepository.findByNome(restauranteNome)).thenReturn(Optional.of(restaurante));

        // When & Then
        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> restauranteService.cadastrarRestaurante(restauranteDTO)
        );

        assertEquals("Restaurante já cadastrado: " + restauranteNome, exception.getMessage());
        verify(restauranteRepository, never()).save(any());
        verify(modelMapper, never()).map(any(), any()); // Nem chegou a mapear
    }

    @Test
    @DisplayName("Cadastrar: Deve lançar exceção quando Nome é nulo (validação)")
    void cadastrarRestaurante_DeveLancarExcecao_QuandoNomeNulo() {
        // Given
        restauranteDTO.setNome(null);
        // Precisamos mockar o mapper ANTES da validação
        when(modelMapper.map(restauranteDTO, Restaurante.class)).thenReturn(restaurante);
        restaurante.setNome(null); // Simula o DTO mapeado

        // When & Then
        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> restauranteService.cadastrarRestaurante(restauranteDTO)
        );

        assertEquals("Nome é obrigatório", exception.getMessage());
        verify(restauranteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cadastrar: Deve lançar exceção quando Taxa de Entrega é negativa (validação)")
    void cadastrarRestaurante_DeveLancarExcecao_QuandoTaxaEntregaNegativa() {
        // Given
        restauranteDTO.setTaxaEntrega(BigDecimal.valueOf(-5.00));
        when(modelMapper.map(restauranteDTO, Restaurante.class)).thenReturn(restaurante);
        restaurante.setTaxaEntrega(BigDecimal.valueOf(-5.00)); // Simula o DTO mapeado

        // When & Then
        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> restauranteService.cadastrarRestaurante(restauranteDTO)
        );

        assertEquals("Taxa de entrega não pode ser negativa", exception.getMessage());
        verify(restauranteRepository, never()).save(any());
    }

    // ==========================================================
    // Testes do buscarRestaurantePorId
    // ==========================================================

    @Test
    @DisplayName("Buscar por ID: Deve lançar exceção quando ID não existe")
    void buscarRestaurantePorId_DeveLancarExcecao_QuandoIdNaoExiste() {
        // Given
        when(restauranteRepository.findById(restauranteId)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> restauranteService.buscarRestaurantePorId(restauranteId)
        );

        assertEquals("Restaurante não encontrado: " + restauranteId, exception.getMessage());
    }

    // ==========================================================
    // Testes do alterarStatusRestaurante
    // ==========================================================

    @Test
    @DisplayName("Alterar Status: Deve desativar um restaurante ativo")
    void alterarStatusRestaurante_DeveDesativarRestauranteAtivo() {
        // Given
        assertTrue(restaurante.getAtivo()); // Garante que começa ativo
        when(restauranteRepository.findById(restauranteId)).thenReturn(Optional.of(restaurante));
        when(restauranteRepository.save(any(Restaurante.class))).thenReturn(restaurante);
        when(modelMapper.map(restaurante, RestauranteResponseDTO.class)).thenReturn(restauranteResponseDTO);

        // When
        restauranteService.alterarStatusRestaurante(restauranteId);

        // Then
        // Verifica se o estado da entidade 'restaurante' foi invertido ANTES de salvar
        assertFalse(restaurante.getAtivo());
        verify(restauranteRepository).save(restaurante);
    }

    @Test
    @DisplayName("Alterar Status: Deve lançar exceção quando ID não existe")
    void alterarStatusRestaurante_DeveLancarExcecao_QuandoIdNaoExiste() {
        // Given
        when(restauranteRepository.findById(restauranteId)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> restauranteService.alterarStatusRestaurante(restauranteId)
        );

        assertEquals("Restaurante não encontrado: " + restauranteId, exception.getMessage());
        verify(restauranteRepository, never()).save(any());
    }

    // ==========================================================
    // Testes do calcularTaxaEntrega (Muitas linhas perdidas aqui!)
    // ==========================================================

    @Test
    @DisplayName("Calcular Taxa: Deve lançar exceção quando Restaurante não existe")
    void calcularTaxaEntrega_DeveLancarExcecao_QuandoRestauranteNaoExiste() {
        // Given
        when(restauranteRepository.findById(restauranteId)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> restauranteService.calcularTaxaEntrega(restauranteId, "12345-000")
        );
        assertEquals("Restaurante não encontrado: " + restauranteId, exception.getMessage());
    }

    @Test
    @DisplayName("Calcular Taxa: Deve lançar exceção quando Restaurante está inativo")
    void calcularTaxaEntrega_DeveLancarExcecao_QuandoRestauranteInativo() {
        // Given
        restaurante.setAtivo(false); // Restaurante inativo
        when(restauranteRepository.findById(restauranteId)).thenReturn(Optional.of(restaurante));

        // When & Then
        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> restauranteService.calcularTaxaEntrega(restauranteId, "12345-000")
        );
        assertEquals("Restaurante não está disponível", exception.getMessage());
    }

    @Test
    @DisplayName("Calcular Taxa: Deve usar taxa base de 5.00 quando taxa do restaurante é nula")
    void calcularTaxaEntrega_DeveUsarTaxaBasePadrao_QuandoTaxaNula() {
        // Given
        restaurante.setTaxaEntrega(null); // Taxa nula
        when(restauranteRepository.findById(restauranteId)).thenReturn(Optional.of(restaurante));
        String cepFinalPar = "12345-002";

        // When
        BigDecimal taxa = restauranteService.calcularTaxaEntrega(restauranteId, cepFinalPar);

        // Then
        // 5.00 (base) + 0.00 (CEP par) = 5.00
        assertEquals(0, BigDecimal.valueOf(5.00).compareTo(taxa));
    }

    @Test
    @DisplayName("Calcular Taxa: Deve somar 5.00 quando CEP tem final ímpar")
    void calcularTaxaEntrega_DeveSomarAdicional_QuandoCepFinalImpar() {
        // Given
        // A taxa do restaurante é 10.00 (do setUp)
        when(restauranteRepository.findById(restauranteId)).thenReturn(Optional.of(restaurante));
        String cepFinalImpar = "12345-003";

        // When
        BigDecimal taxa = restauranteService.calcularTaxaEntrega(restauranteId, cepFinalImpar);

        // Then
        // 10.00 (restaurante) + 5.00 (CEP ímpar) = 15.00
        assertEquals(0, BigDecimal.valueOf(15.00).compareTo(taxa));
    }

    @Test
    @DisplayName("Calcular Taxa: Não deve somar adicional quando CEP tem final par")
    void calcularTaxaEntrega_NaoDeveSomarAdicional_QuandoCepFinalPar() {
        // Given
        // A taxa do restaurante é 10.00 (do setUp)
        when(restauranteRepository.findById(restauranteId)).thenReturn(Optional.of(restaurante));
        String cepFinalPar = "12345-008";

        // When
        BigDecimal taxa = restauranteService.calcularTaxaEntrega(restauranteId, cepFinalPar);

        // Then
        // 10.00 (restaurante) + 0.00 (CEP par) = 10.00
        assertEquals(0, BigDecimal.valueOf(10.00).compareTo(taxa));
    }
}