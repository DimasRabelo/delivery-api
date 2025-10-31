package com.deliverytech.delivery.service.impl;

import com.deliverytech.delivery.dto.ProdutoDTO;
import com.deliverytech.delivery.dto.response.ProdutoResponseDTO;
import com.deliverytech.delivery.entity.Produto;
import com.deliverytech.delivery.entity.Restaurante;
import com.deliverytech.delivery.exception.BusinessException;
import com.deliverytech.delivery.exception.ConflictException; 
import com.deliverytech.delivery.exception.EntityNotFoundException;
import com.deliverytech.delivery.repository.ProdutoRepository;
import com.deliverytech.delivery.repository.RestauranteRepository;
import com.deliverytech.delivery.service.ProdutoService;
import com.deliverytech.delivery.security.jwt.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;


import java.util.List;
import java.util.stream.Collectors;

@Service("produtoService")
@Transactional
public class ProdutoServiceImpl implements ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private RestauranteRepository restauranteRepository;

    // ===========================
    // CADASTRAR PRODUTO
    // ===========================
    @Override
    public ProdutoResponseDTO cadastrarProduto(ProdutoDTO dto) {
        // Validações do DTO
        if (dto.getCategoria() == null || dto.getCategoria().isEmpty()) {
            throw new IllegalArgumentException("Categoria é obrigatória");
        }
        if (dto.getDescricao() == null || dto.getDescricao().length() < 10) {
            throw new IllegalArgumentException("Descrição deve ter entre 10 e 500 caracteres");
        }
        if (dto.getRestauranteId() == null) {
            throw new IllegalArgumentException("Restaurante ID é obrigatório");
        }

        // Busca Restaurante
        Restaurante restaurante = restauranteRepository.findById(dto.getRestauranteId())
                .orElseThrow(() -> new EntityNotFoundException("Restaurante não encontrado"));

        // Lógica de Conflito 
        boolean exists = produtoRepository.existsByNomeAndRestauranteId(dto.getNome(), dto.getRestauranteId());
        if (exists) {
            throw new ConflictException("Produto já existe para este restaurante", "nome", dto.getNome());
        }

        // Cria a entidade Produto
        Produto produto = new Produto();
        produto.setNome(dto.getNome());
        produto.setDescricao(dto.getDescricao());
        produto.setPreco(dto.getPreco());
        produto.setCategoria(dto.getCategoria());
        produto.setDisponivel(true); 
        produto.setRestaurante(restaurante);
        
        // Adiciona o estoque (a nova lógica)
        produto.setEstoque(dto.getEstoque());

      // Salva no banco e captura a entidade gerenciada (com ID)
Produto produtoSalvo = produtoRepository.save(produto);

// Retorna o DTO de Resposta (que já lê o estoque)
return new ProdutoResponseDTO(produtoSalvo); // Use a variável 'produtoSalvo'
    }

    // ===========================
    // BUSCAR PRODUTO POR ID
    // ===========================
    @Override
    @Transactional(readOnly = true)
    public ProdutoResponseDTO buscarProdutoPorId(Long id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + id));

        if (!produto.getDisponivel()) {
            throw new BusinessException("Produto não está disponível");
        }

        ProdutoResponseDTO dto = new ProdutoResponseDTO(produto);
        dto.setRestauranteId(produto.getRestaurante() != null ? produto.getRestaurante().getId() : null);
        return dto;
    }

    // ===========================
    // BUSCAR PRODUTOS POR NOME
    // ===========================
    @Override
    @Transactional(readOnly = true)
    public List<ProdutoResponseDTO> buscarProdutosPorNome(String nome) {
        List<Produto> produtos = produtoRepository.findByNomeContainingIgnoreCaseAndDisponivelTrue(nome);

        return produtos.stream()
                .map(p -> {
                    ProdutoResponseDTO dto = new ProdutoResponseDTO(p);
                    dto.setRestauranteId(p.getRestaurante() != null ? p.getRestaurante().getId() : null);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // ===========================
    // BUSCAR PRODUTOS POR RESTAURANTE
    // ===========================
    @Override
    @Transactional(readOnly = true)
    public List<ProdutoResponseDTO> buscarProdutosPorRestaurante(Long restauranteId, Boolean disponivel) {
        List<Produto> produtos;
        if (disponivel != null) {
            produtos = produtoRepository.findByRestauranteIdAndDisponivel(restauranteId, disponivel);
        } else {
            produtos = produtoRepository.findByRestauranteIdAndDisponivel(restauranteId, true);
        }

        return produtos.stream()
                .map(p -> {
                    ProdutoResponseDTO dto = new ProdutoResponseDTO(p);
                    dto.setRestauranteId(p.getRestaurante() != null ? p.getRestaurante().getId() : null);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // ===========================
    // ATUALIZAR PRODUTO
    // ===========================
    @Override
    public ProdutoResponseDTO atualizarProduto(Long id, ProdutoDTO dto) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + id));

        Restaurante restaurante = restauranteRepository.findById(dto.getRestauranteId())
                .orElseThrow(() -> new EntityNotFoundException("Restaurante não encontrado: " + dto.getRestauranteId()));

        produto.setNome(dto.getNome());
        produto.setDescricao(dto.getDescricao());
        produto.setPreco(dto.getPreco());
        produto.setCategoria(dto.getCategoria());
        if (dto.getDisponivel() != null) {
            produto.setDisponivel(dto.getDisponivel());
        }
        produto.setRestaurante(restaurante);
        produto.setEstoque(dto.getEstoque());

        Produto atualizado = produtoRepository.save(produto);

        ProdutoResponseDTO responseDTO = new ProdutoResponseDTO(atualizado);
        responseDTO.setRestauranteId(atualizado.getRestaurante() != null ? atualizado.getRestaurante().getId() : null);
        return responseDTO;
    }

    // ===========================
    // ALTERAR DISPONIBILIDADE
    // ===========================
    @Override
    public ProdutoResponseDTO alterarDisponibilidade(Long id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));

        produto.setDisponivel(!produto.getDisponivel());
        produtoRepository.save(produto);

        ProdutoResponseDTO dto = new ProdutoResponseDTO(produto);
        dto.setRestauranteId(produto.getRestaurante() != null ? produto.getRestaurante().getId() : null);
        return dto;
    }

    // ===========================
    // REMOVER PRODUTO
    // ===========================
    @Override
    public void removerProduto(Long id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));

        produtoRepository.delete(produto);
    }

    // ===========================
    // BUSCAR PRODUTOS POR CATEGORIA
    // ===========================
    @Override
    @Transactional(readOnly = true)
    public List<ProdutoResponseDTO> buscarProdutosPorCategoria(String categoria) {
        List<Produto> produtos = produtoRepository.findByCategoriaAndDisponivelTrue(categoria);
        return produtos.stream()
                .map(p -> {
                    ProdutoResponseDTO dto = new ProdutoResponseDTO(p);
                    dto.setRestauranteId(p.getRestaurante() != null ? p.getRestaurante().getId() : null);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // ===========================
    // VERIFICA SE O USUÁRIO É DONO DO PRODUTO
    // ===========================
    @Override
    public boolean isOwner(Long produtoId) {
        Produto produto = produtoRepository.findById(produtoId).orElse(null);
        if (produto == null) return false;

        Long restauranteLogadoId = SecurityUtils.getCurrentRestauranteId();
        if (restauranteLogadoId == null) return false;

        return produto.getRestaurante() != null &&
               produto.getRestaurante().getId().equals(restauranteLogadoId);
    }

    // ===========================
    // LISTAR PRODUTOS (PAGINADO COM FILTROS)
    // ===========================
    @Override
    @Transactional(readOnly = true)
    public Page<ProdutoResponseDTO> listarProdutos(Pageable pageable, Long restauranteId, String categoria, Boolean disponivel) {
        
        // 1. Cria uma "Specification" base que não filtra nada
        Specification<Produto> spec = Specification.not(null);

        // 2. Adiciona os filtros dinamicamente, se eles existirem
        if (restauranteId != null) {
            spec = spec.and((root, query, cb) -> 
                cb.equal(root.get("restaurante").get("id"), restauranteId)
            );
        }

        if (categoria != null && !categoria.isBlank()) {
            // Usando 'like' para busca parcial e case-insensitive
            spec = spec.and((root, query, cb) -> 
                cb.like(cb.lower(root.get("categoria")), "%" + categoria.toLowerCase() + "%")
            );
        }

        if (disponivel != null) {
            spec = spec.and((root, query, cb) -> 
                cb.equal(root.get("disponivel"), disponivel)
            );
        }

        // 3. Executa a busca no repositório com os filtros e a paginação
        Page<Produto> paginaDeProdutos = produtoRepository.findAll(spec, pageable);

        // 4. Mapeia a Page<Produto> para Page<ProdutoResponseDTO>
        //    (Replicando seu padrão de setar o restauranteId manualmente)
        return paginaDeProdutos.map(produto -> {
            ProdutoResponseDTO dto = new ProdutoResponseDTO(produto);
            dto.setRestauranteId(produto.getRestaurante() != null ? produto.getRestaurante().getId() : null);
            return dto;
        });
    }
}