package com.deliverytech.delivery.service.impl;

import com.deliverytech.delivery.dto.ProdutoDTO;
import com.deliverytech.delivery.dto.GrupoOpcionalDTO; // IMPORT ADICIONADO
import com.deliverytech.delivery.dto.ItemOpcionalDTO; // IMPORT ADICIONADO
import com.deliverytech.delivery.dto.response.ProdutoResponseDTO;
import com.deliverytech.delivery.entity.Produto;
import com.deliverytech.delivery.entity.Restaurante;
import com.deliverytech.delivery.entity.GrupoOpcional; // IMPORT ADICIONADO
import com.deliverytech.delivery.entity.ItemOpcional; // IMPORT ADICIONADO
//import com.deliverytech.delivery.exception.BusinessException;
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
//import java.util.ArrayList; // IMPORT ADICIONADO
import java.util.stream.Collectors;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

@Service("produtoService")
@Transactional
public class ProdutoServiceImpl implements ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private RestauranteRepository restauranteRepository;
    
    // (Não precisamos dos repositórios de Opcionais aqui, 
    // pois o Cascade fará o salvamento)

    /**
     * Cadastra um novo produto (VERSÃO REFATORADA).
     * Agora salva o 'precoBase' e a árvore de 'gruposOpcionais'.
     */
    @Override
    @CacheEvict(value = "produtos", allEntries = true) // Limpa o cache de produtos
    public ProdutoResponseDTO cadastrarProduto(ProdutoDTO dto) {
        
        // (Validações de DTO - seu código original)
        if (dto.getRestauranteId() == null) {
            throw new IllegalArgumentException("Restaurante ID é obrigatório");
        }
        
        Restaurante restaurante = restauranteRepository.findById(dto.getRestauranteId())
                .orElseThrow(() -> new EntityNotFoundException("Restaurante não encontrado"));

        boolean exists = produtoRepository.existsByNomeAndRestauranteId(dto.getNome(), dto.getRestauranteId());
        if (exists) {
            throw new ConflictException("Produto já existe para este restaurante", "nome", dto.getNome());
        }

        // 1. Mapeia os dados básicos do Produto
        Produto produto = new Produto();
        produto.setNome(dto.getNome());
        produto.setDescricao(dto.getDescricao());
        produto.setCategoria(dto.getCategoria());
        produto.setDisponivel(true); // Padrão
        produto.setEstoque(dto.getEstoque());
        produto.setRestaurante(restaurante);

        // --- CORREÇÃO (GARGALO 2) ---
        produto.setPrecoBase(dto.getPrecoBase()); // <-- MUDOU (era 'preco')

        // 2. Constrói a árvore de Opcionais (Gargalo 2)
        if (dto.getGruposOpcionais() != null) {
            for (GrupoOpcionalDTO grupoDTO : dto.getGruposOpcionais()) {
                GrupoOpcional grupo = new GrupoOpcional();
                grupo.setNome(grupoDTO.getNome());
                grupo.setMinSelecao(grupoDTO.getMinSelecao());
                grupo.setMaxSelecao(grupoDTO.getMaxSelecao());
                grupo.setProduto(produto); // Linka o grupo ao produto "pai"

                if (grupoDTO.getItensOpcionais() != null) {
                    for (ItemOpcionalDTO itemDTO : grupoDTO.getItensOpcionais()) {
                        ItemOpcional item = new ItemOpcional();
                        item.setNome(itemDTO.getNome());
                        item.setPrecoAdicional(itemDTO.getPrecoAdicional());
                        item.setGrupoOpcional(grupo); // Linka o item ao grupo "pai"
                        grupo.getItensOpcionais().add(item); // Adiciona o item ao grupo
                    }
                }
                produto.getGruposOpcionais().add(grupo); // Adiciona o grupo ao produto
            }
        }

        Produto produtoSalvo = produtoRepository.save(produto); // O Cascade salva tudo

        return new ProdutoResponseDTO(produtoSalvo); // O novo DTO de resposta lida com a árvore
    }

    /**
     * Atualiza um produto (VERSÃO REFATORADA).
     * Usa 'orphanRemoval=true' para limpar e recriar os opcionais.
     */
    @Override
    @CacheEvict(value = "produtos", key = "#id")
    public ProdutoResponseDTO atualizarProduto(Long id, ProdutoDTO dto) {
        
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + id));

        Restaurante restaurante = restauranteRepository.findById(dto.getRestauranteId())
                .orElseThrow(() -> new EntityNotFoundException("Restaurante não encontrado: " + dto.getRestauranteId()));

        // 1. Atualiza os dados básicos
        produto.setNome(dto.getNome());
        produto.setDescricao(dto.getDescricao());
        produto.setCategoria(dto.getCategoria());
        produto.setEstoque(dto.getEstoque());
        produto.setRestaurante(restaurante);

        // --- CORREÇÃO (GARGALO 2) ---
        produto.setPrecoBase(dto.getPrecoBase()); // <-- MUDOU (era 'preco')
        
        // (Seu DTO antigo tinha 'disponivel', o novo não, mas o 'atualizar' deveria ter)
        // if (dto.getDisponivel() != null) { 
        //     produto.setDisponivel(dto.getDisponivel());
        // }

        // 2. Atualiza a árvore de Opcionais (Gargalo 2)
        // A forma mais simples (usando orphanRemoval=true na Entidade Produto)
        // é limpar a lista antiga e adicionar os novos.
        
        produto.getGruposOpcionais().clear(); // <-- Limpa a lista (Cascade remove os antigos)

        if (dto.getGruposOpcionais() != null) {
            for (GrupoOpcionalDTO grupoDTO : dto.getGruposOpcionais()) {
                GrupoOpcional grupo = new GrupoOpcional();
                grupo.setNome(grupoDTO.getNome());
                grupo.setMinSelecao(grupoDTO.getMinSelecao());
                grupo.setMaxSelecao(grupoDTO.getMaxSelecao());
                grupo.setProduto(produto);

                if (grupoDTO.getItensOpcionais() != null) {
                    for (ItemOpcionalDTO itemDTO : grupoDTO.getItensOpcionais()) {
                        ItemOpcional item = new ItemOpcional();
                        item.setNome(itemDTO.getNome());
                        item.setPrecoAdicional(itemDTO.getPrecoAdicional());
                        item.setGrupoOpcional(grupo);
                        grupo.getItensOpcionais().add(item);
                    }
                }
                produto.getGruposOpcionais().add(grupo); // Adiciona o grupo (novo)
            }
        }

        Produto atualizado = produtoRepository.save(produto);

        return new ProdutoResponseDTO(atualizado);
    }


    // ==========================================================
    // MÉTODOS DE BUSCA (A maioria deve funcionar agora)
    // O construtor 'new ProdutoResponseDTO(produto)' foi refatorado
    // para lidar com a nova estrutura (precoBase e opcionais).
    // ==========================================================

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "produtos", key = "#id")
    public ProdutoResponseDTO buscarProdutoPorId(Long id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + id));

        // Sua lógica original de 'disponivel' foi movida para o 'listar'
        // if (!produto.getDisponivel()) {
        //     throw new BusinessException("Produto não está disponível");
        // }

        // O novo construtor do DTO de Resposta faz todo o trabalho de mapeamento
        return new ProdutoResponseDTO(produto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProdutoResponseDTO> buscarProdutosPorNome(String nome) {
        List<Produto> produtos = produtoRepository.findByNomeContainingIgnoreCaseAndDisponivelTrue(nome);
        return produtos.stream()
                .map(ProdutoResponseDTO::new) // Usa o construtor refatorado
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProdutoResponseDTO> buscarProdutosPorRestaurante(Long restauranteId, Boolean disponivel) {
        List<Produto> produtos;
        if (disponivel != null) {
            produtos = produtoRepository.findByRestauranteIdAndDisponivel(restauranteId, disponivel);
        } else {
            // Padrão: buscar todos (incluindo indisponíveis, para o gerente ver)
            produtos = produtoRepository.findByRestauranteId(restauranteId);
        }

        return produtos.stream()
                .map(ProdutoResponseDTO::new) // Usa o construtor refatorado
                .collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = "produtos", key = "#id")
    public ProdutoResponseDTO alterarDisponibilidade(Long id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));

        produto.setDisponivel(produto.getDisponivel() == null ? true : !produto.getDisponivel());
        produtoRepository.save(produto);

        return new ProdutoResponseDTO(produto);
    }

    @Override
    @CacheEvict(value = "produtos", key = "#id")
    public void removerProduto(Long id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));

        // (Aqui você pode querer verificar se o produto está em algum Pedido antes de deletar)
        
        produtoRepository.delete(produto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProdutoResponseDTO> buscarProdutosPorCategoria(String categoria) {
        List<Produto> produtos = produtoRepository.findByCategoriaAndDisponivelTrue(categoria);
        return produtos.stream()
                .map(ProdutoResponseDTO::new) // Usa o construtor refatorado
                .collect(Collectors.toList());
    }

    @Override
    public boolean isOwner(Long produtoId) {
        // Esta lógica está OK
        Produto produto = produtoRepository.findById(produtoId).orElse(null);
        if (produto == null) return false;
        Long restauranteLogadoId = SecurityUtils.getCurrentRestauranteId();
        if (restauranteLogadoId == null) return false;
        return produto.getRestaurante() != null &&
               produto.getRestaurante().getId().equals(restauranteLogadoId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProdutoResponseDTO> listarProdutos(Pageable pageable, Long restauranteId, String categoria, Boolean disponivel) {
        // Esta lógica de Specification está OK
        Specification<Produto> spec = Specification.not(null);
        // ... (seu código de spec) ...
        Page<Produto> paginaDeProdutos = produtoRepository.findAll(spec, pageable);
        return paginaDeProdutos.map(ProdutoResponseDTO::new); // Usa o construtor refatorado
    }
}