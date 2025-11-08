package com.deliverytech.delivery.service.impl;

import com.deliverytech.delivery.dto.request.GrupoOpcionalDTO;
import com.deliverytech.delivery.dto.request.ItemOpcionalDTO;
import com.deliverytech.delivery.dto.request.ProdutoDTO;
import com.deliverytech.delivery.dto.response.ProdutoResponseDTO;
import com.deliverytech.delivery.entity.Produto;
import com.deliverytech.delivery.entity.Restaurante;
import com.deliverytech.delivery.entity.GrupoOpcional;
import com.deliverytech.delivery.entity.ItemOpcional;
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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

@Service("produtoService")
@Transactional
public class ProdutoServiceImpl implements ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private RestauranteRepository restauranteRepository;
    
    // (Método cadastrarProduto... OK)
    @Override
    @CacheEvict(value = "produtos", allEntries = true)
    public ProdutoResponseDTO cadastrarProduto(ProdutoDTO dto) {
        if (dto.getRestauranteId() == null) {
            throw new IllegalArgumentException("Restaurante ID é obrigatório");
        }
        Restaurante restaurante = restauranteRepository.findById(dto.getRestauranteId())
                .orElseThrow(() -> new EntityNotFoundException("Restaurante não encontrado"));
        boolean exists = produtoRepository.existsByNomeAndRestauranteId(dto.getNome(), dto.getRestauranteId());
        if (exists) {
            throw new ConflictException("Produto já existe para este restaurante", "nome", dto.getNome());
        }
        Produto produto = new Produto();
        produto.setNome(dto.getNome());
        produto.setDescricao(dto.getDescricao());
        produto.setCategoria(dto.getCategoria());
        produto.setDisponivel(true);
        produto.setEstoque(dto.getEstoque());
        produto.setRestaurante(restaurante);
        produto.setPrecoBase(dto.getPrecoBase()); 
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
                produto.getGruposOpcionais().add(grupo); 
            }
        }
        Produto produtoSalvo = produtoRepository.save(produto); 
        return new ProdutoResponseDTO(produtoSalvo); 
    }

    // (Método atualizarProduto... OK)
    @Override
    @CacheEvict(value = "produtos", key = "#id")
    public ProdutoResponseDTO atualizarProduto(Long id, ProdutoDTO dto) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + id));
        Restaurante restaurante = restauranteRepository.findById(dto.getRestauranteId())
                .orElseThrow(() -> new EntityNotFoundException("Restaurante não encontrado: " + dto.getRestauranteId()));
        produto.setNome(dto.getNome());
        produto.setDescricao(dto.getDescricao());
        produto.setCategoria(dto.getCategoria());
        produto.setEstoque(dto.getEstoque());
        produto.setRestaurante(restaurante);
        produto.setPrecoBase(dto.getPrecoBase());
        produto.getGruposOpcionais().clear(); 
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
                produto.getGruposOpcionais().add(grupo); 
            }
        }
        Produto atualizado = produtoRepository.save(produto);
        return new ProdutoResponseDTO(atualizado);
    }

    // ==========================================================
    // --- MÉTODO buscarProdutoPorId (A CORREÇÃO DO ERRO 500) ---
    // ==========================================================
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "produtos", key = "#id")
    public ProdutoResponseDTO buscarProdutoPorId(Long id) {
        // Antes: produtoRepository.findById(id)
        // CORREÇÃO: Usamos a nova query com JOIN FETCH
        Produto produto = produtoRepository.findProdutoCompletoById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + id));
        
        // Agora, quando o construtor for chamado, 'produto.getGruposOpcionais()'
        // e 'grupo.getItensOpcionais()' já estarão carregados,
        // evitando a LazyInitializationException.
        return new ProdutoResponseDTO(produto);
    }
    // ==========================================================
    // FIM DA CORREÇÃO
    // ==========================================================

    // (Método buscarProdutosPorNome... OK)
    @Override
    @Transactional(readOnly = true)
    public List<ProdutoResponseDTO> buscarProdutosPorNome(String nome) {
        List<Produto> produtos = produtoRepository.findByNomeContainingIgnoreCaseAndDisponivelTrue(nome);
        return produtos.stream()
                .map(ProdutoResponseDTO::new)
                .collect(Collectors.toList());
    }

    // (Método buscarProdutosPorRestaurante... OK)
    @Override
    @Transactional(readOnly = true)
    public List<ProdutoResponseDTO> buscarProdutosPorRestaurante(Long restauranteId, Boolean disponivel) {
        List<Produto> produtos;
        if (disponivel == null || disponivel == true) {
            produtos = produtoRepository.findByRestauranteIdAndDisponivelTrue(restauranteId);
        } else {
            produtos = produtoRepository.findByRestauranteIdAndDisponivel(restauranteId, false);
        }
        return produtos.stream()
                .map(ProdutoResponseDTO::new)
                .collect(Collectors.toList());
    }
    
    // (Método alterarDisponibilidade... OK)
    @Override
    @CacheEvict(value = "produtos", key = "#id")
    public ProdutoResponseDTO alterarDisponibilidade(Long id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
        produto.setDisponivel(produto.getDisponivel() == null ? true : !produto.getDisponivel());
        produtoRepository.save(produto);
        return new ProdutoResponseDTO(produto);
    }

    // (Método removerProduto... OK)
    @Override
    @CacheEvict(value = "produtos", key = "#id")
    public void removerProduto(Long id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
        produtoRepository.delete(produto);
    }

    // (Método buscarProdutosPorCategoria... OK)
    @Override
    @Transactional(readOnly = true)
    public List<ProdutoResponseDTO> buscarProdutosPorCategoria(String categoria) {
        List<Produto> produtos = produtoRepository.findByCategoriaAndDisponivelTrue(categoria);
        return produtos.stream()
                .map(ProdutoResponseDTO::new) 
                .collect(Collectors.toList());
    }

    // (Método isOwner... OK)
    @Override
    public boolean isOwner(Long produtoId) {
        Produto produto = produtoRepository.findById(produtoId).orElse(null);
        if (produto == null) return false;
        Long restauranteLogadoId = SecurityUtils.getCurrentRestauranteId();
        if (restauranteLogadoId == null) return false;
        return produto.getRestaurante() != null &&
               produto.getRestaurante().getId().equals(restauranteLogadoId);
    }

    // (Método listarProdutos... OK)
    @Override
    @Transactional(readOnly = true)
    public Page<ProdutoResponseDTO> listarProdutos(Pageable pageable, Long restauranteId, String categoria, Boolean disponivel) {
        Specification<Produto> spec = Specification.not(null);
        // ... (seu código de spec) ...
        Page<Produto> paginaDeProdutos = produtoRepository.findAll(spec, pageable);
        return paginaDeProdutos.map(ProdutoResponseDTO::new);
    }
}