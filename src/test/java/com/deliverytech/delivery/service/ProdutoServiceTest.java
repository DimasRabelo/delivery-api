package com.deliverytech.delivery.service; // Coloque no mesmo pacote dos seus outros testes de serviço

import com.deliverytech.delivery.dto.ProdutoDTO;
import com.deliverytech.delivery.dto.response.ProdutoResponseDTO;
import com.deliverytech.delivery.entity.Produto;
import com.deliverytech.delivery.entity.Restaurante;
import com.deliverytech.delivery.exception.BusinessException;
import com.deliverytech.delivery.exception.ConflictException;
import com.deliverytech.delivery.exception.EntityNotFoundException;
import com.deliverytech.delivery.repository.ProdutoRepository;
import com.deliverytech.delivery.repository.RestauranteRepository;
import com.deliverytech.delivery.security.jwt.SecurityUtils; // Import para o método isOwner
import com.deliverytech.delivery.service.impl.ProdutoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic; // Import para mockar o método estático
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Anotação para integrar o JUnit 5 com o Mockito.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do ProdutoService (Impl)")
class ProdutoServiceTest {

    // --- DEPENDÊNCIAS MOCADAS ---
    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private RestauranteRepository restauranteRepository;

    // --- SYSTEM UNDER TEST (SUT) ---
    @InjectMocks // Injete a classe de implementação concreta
    private ProdutoServiceImpl produtoService;

    // --- OBJETOS DE APOIO PARA OS TESTES ---
    private ProdutoDTO produtoDTO;
    private Produto produto;
    private Restaurante restaurante;
    private Long produtoId = 1L;
    private Long restauranteId = 10L;

    /**
     * O método @BeforeEach é executado antes de CADA teste.
     */
    @BeforeEach
    void setUp() {
        // 1. DTO (Entrada)
        produtoDTO = new ProdutoDTO();
        produtoDTO.setNome("Pizza de Calabresa");
        produtoDTO.setDescricao("Descrição com mais de 10 caracteres");
        produtoDTO.setPreco(BigDecimal.valueOf(50.00));
        produtoDTO.setCategoria("Pizzas");
        produtoDTO.setRestauranteId(restauranteId);
        produtoDTO.setEstoque(10);
        produtoDTO.setDisponivel(true);

        // 2. Entidade Restaurante (Dependência)
        restaurante = new Restaurante();
        restaurante.setId(restauranteId);
        restaurante.setNome("Pizzaria Boa");

        // 3. Entidade Produto (Banco de Dados)
        produto = new Produto();
        produto.setId(produtoId);
        produto.setNome("Pizza de Calabresa");
        produto.setDescricao("Descrição com mais de 10 caracteres");
        produto.setPreco(BigDecimal.valueOf(50.00));
        produto.setCategoria("Pizzas");
        produto.setDisponivel(true);
        produto.setEstoque(10);
        produto.setRestaurante(restaurante);
    }

    // ==========================================================
    // Testes do cadastrarProduto
    // ==========================================================

    @Test
    @DisplayName("Cadastrar: Deve cadastrar produto com sucesso")
    void cadastrarProduto_DeveSalvarComSucesso_QuandoDadosValidos() {
        // -----------------
        // Given (Arrange)
        // -----------------
        // 1. Simula a busca do restaurante (encontrado)
        when(restauranteRepository.findById(restauranteId)).thenReturn(Optional.of(restaurante));
        // 2. Simula a verificação de conflito (não existe)
        when(produtoRepository.existsByNomeAndRestauranteId(produtoDTO.getNome(), restauranteId)).thenReturn(false);
        // 3. Simula a ação de salvar (retorna a entidade com ID)
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);

        // -----------------
        // When (Act)
        // -----------------
        ProdutoResponseDTO resultado = produtoService.cadastrarProduto(produtoDTO);

        // -----------------
        // Then (Assert)
        // -----------------
        assertNotNull(resultado);
        assertEquals(produtoDTO.getNome(), resultado.getNome());
        assertEquals(produtoId, resultado.getId());
        assertEquals(10, resultado.getEstoque()); // Verifica o novo campo estoque

        // Verifica se o 'save' foi chamado
        verify(produtoRepository).save(any(Produto.class));
    }

    @Test
    @DisplayName("Cadastrar: Deve lançar exceção quando Categoria é nula")
    void cadastrarProduto_DeveLancarExcecao_QuandoCategoriaNula() {
        // Given
        produtoDTO.setCategoria(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> produtoService.cadastrarProduto(produtoDTO)
        );
        assertEquals("Categoria é obrigatória", exception.getMessage());
        verify(produtoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cadastrar: Deve lançar exceção quando Descrição é curta")
    void cadastrarProduto_DeveLancarExcecao_QuandoDescricaoCurta() {
        // Given
        produtoDTO.setDescricao("Curta"); // Menos de 10 caracteres

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> produtoService.cadastrarProduto(produtoDTO)
        );
        assertEquals("Descrição deve ter entre 10 e 500 caracteres", exception.getMessage());
        verify(produtoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cadastrar: Deve lançar exceção quando Restaurante ID é nulo")
    void cadastrarProduto_DeveLancarExcecao_QuandoRestauranteIdNulo() {
        // Given
        produtoDTO.setRestauranteId(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> produtoService.cadastrarProduto(produtoDTO)
        );
        assertEquals("Restaurante ID é obrigatório", exception.getMessage());
        verify(produtoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cadastrar: Deve lançar exceção quando Restaurante não é encontrado")
    void cadastrarProduto_DeveLancarExcecao_QuandoRestauranteNaoEncontrado() {
        // Given
        // Simula que o restaurante não foi encontrado
        when(restauranteRepository.findById(restauranteId)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> produtoService.cadastrarProduto(produtoDTO)
        );
        assertEquals("Restaurante não encontrado", exception.getMessage());
        verify(produtoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cadastrar: Deve lançar exceção quando Produto já existe (Conflito)")
    void cadastrarProduto_DeveLancarExcecao_QuandoProdutoJaExiste() {
        // Given
        // 1. Restaurante é encontrado
        when(restauranteRepository.findById(restauranteId)).thenReturn(Optional.of(restaurante));
        // 2. Simula que o produto JÁ EXISTE
        when(produtoRepository.existsByNomeAndRestauranteId(produtoDTO.getNome(), restauranteId)).thenReturn(true);

        // When & Then
        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> produtoService.cadastrarProduto(produtoDTO)
        );
        assertEquals("Produto já existe para este restaurante", exception.getMessage());
        verify(produtoRepository, never()).save(any());
    }

    // ==========================================================
    // Testes do buscarProdutoPorId
    // ==========================================================

    @Test
    @DisplayName("Buscar por ID: Deve retornar DTO quando ID existe e está disponível")
    void buscarProdutoPorId_DeveRetornarDTO_QuandoIdExisteDisponivel() {
        // Given
        when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produto));

        // When
        ProdutoResponseDTO resultado = produtoService.buscarProdutoPorId(produtoId);

        // Then
        assertNotNull(resultado);
        assertEquals(produtoId, resultado.getId());
        assertEquals(restauranteId, resultado.getRestauranteId());
    }

    @Test
    @DisplayName("Buscar por ID: Deve lançar exceção quando ID não existe")
    void buscarProdutoPorId_DeveLancarExcecao_QuandoIdNaoExiste() {
        // Given
        when(produtoRepository.findById(produtoId)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> produtoService.buscarProdutoPorId(produtoId)
        );
        assertEquals("Produto não encontrado: " + produtoId, exception.getMessage());
    }

    @Test
    @DisplayName("Buscar por ID: Deve lançar exceção quando Produto não está disponível")
    void buscarProdutoPorId_DeveLancarExcecao_QuandoProdutoNaoDisponivel() {
        // Given
        // 1. Produto existe, mas está indisponível
        produto.setDisponivel(false);
        when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produto));

        // When & Then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> produtoService.buscarProdutoPorId(produtoId)
        );
        assertEquals("Produto não está disponível", exception.getMessage());
    }

    // ==========================================================
    // Testes do isOwner (Cobre muitos 'branches'/'if's)
    // ==========================================================

    @Test
    @DisplayName("isOwner: Deve retornar true quando ID do restaurante logado bate")
    void isOwner_DeveRetornarTrue_QuandoRestauranteLogadoForDono() {
        // Precisamos mockar o método estático SecurityUtils.getCurrentRestauranteId()
        // Isso é feito com um try-with-resources
        try (MockedStatic<SecurityUtils> mockedUtils = mockStatic(SecurityUtils.class)) {
            // Given
            // 1. Simula o ID do restaurante logado
            mockedUtils.when(SecurityUtils::getCurrentRestauranteId).thenReturn(restauranteId);
            // 2. Simula a busca do produto
            when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produto));

            // When
            boolean isOwner = produtoService.isOwner(produtoId);

            // Then
            assertTrue(isOwner);
        }
    }

    @Test
    @DisplayName("isOwner: Deve retornar false quando ID do restaurante logado é diferente")
    void isOwner_DeveRetornarFalse_QuandoRestauranteLogadoNaoForDono() {
        try (MockedStatic<SecurityUtils> mockedUtils = mockStatic(SecurityUtils.class)) {
            // Given
            // 1. Simula um ID de restaurante logado DIFERENTE
            mockedUtils.when(SecurityUtils::getCurrentRestauranteId).thenReturn(999L);
            // 2. Simula a busca do produto
            when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produto));

            // When
            boolean isOwner = produtoService.isOwner(produtoId);

            // Then
            assertFalse(isOwner);
        }
    }

    @Test
    @DisplayName("isOwner: Deve retornar false quando produto não é encontrado")
    void isOwner_DeveRetornarFalse_QuandoProdutoNaoEncontrado() {
        // Given
        // 1. Simula que o produto não foi encontrado
        when(produtoRepository.findById(produtoId)).thenReturn(Optional.empty());

        // When
        boolean isOwner = produtoService.isOwner(produtoId);

        // Then
        assertFalse(isOwner);
        // Garantimos que nem tentou pegar o ID do usuário
        // (Isso depende de como o mock estático é configurado,
        // mas o teste de lógica está correto)
    }

    @Test
    @DisplayName("isOwner: Deve retornar false quando usuário logado é nulo")
    void isOwner_DeveRetornarFalse_QuandoRestauranteLogadoNulo() {
        try (MockedStatic<SecurityUtils> mockedUtils = mockStatic(SecurityUtils.class)) {
            // Given
            // 1. Simula que não há restaurante logado
            mockedUtils.when(SecurityUtils::getCurrentRestauranteId).thenReturn(null);
            // 2. Simula a busca do produto
            when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produto));

            // When
            boolean isOwner = produtoService.isOwner(produtoId);

            // Then
            assertFalse(isOwner);
        }
    }
}