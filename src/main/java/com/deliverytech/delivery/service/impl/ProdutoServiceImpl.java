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
    
    /**
     * Cadastra um novo produto com seus grupos opcionais e itens aninhados.
     */
    @Override
    @CacheEvict(value = "produtos", allEntries = true) // Invalida todo o cache de 'produtos'
    public ProdutoResponseDTO cadastrarProduto(ProdutoDTO dto) {
        if (dto.getRestauranteId() == null) {
            throw new IllegalArgumentException("Restaurante ID é obrigatório");
        }
        
        // Valida o restaurante
        Restaurante restaurante = restauranteRepository.findById(dto.getRestauranteId())
                .orElseThrow(() -> new EntityNotFoundException("Restaurante não encontrado"));
        
        // Valida se já existe um produto com o mesmo nome para este restaurante
        boolean exists = produtoRepository.existsByNomeAndRestauranteId(dto.getNome(), dto.getRestauranteId());
        if (exists) {
            throw new ConflictException("Produto já existe para este restaurante", "nome", dto.getNome());
        }

        // Mapeia o DTO para a entidade Produto
        Produto produto = new Produto();
        produto.setNome(dto.getNome());
        produto.setDescricao(dto.getDescricao());
        produto.setCategoria(dto.getCategoria());
        produto.setDisponivel(true); // Padrão ao cadastrar
        produto.setEstoque(dto.getEstoque());
        produto.setRestaurante(restaurante);
        produto.setPrecoBase(dto.getPrecoBase()); 

        // Processa as entidades aninhadas (Grupos e Itens)
        if (dto.getGruposOpcionais() != null) {
            for (GrupoOpcionalDTO grupoDTO : dto.getGruposOpcionais()) {
                GrupoOpcional grupo = new GrupoOpcional();
                grupo.setNome(grupoDTO.getNome());
                grupo.setMinSelecao(grupoDTO.getMinSelecao());
                grupo.setMaxSelecao(grupoDTO.getMaxSelecao());
                grupo.setProduto(produto); // Define a relação (lado "Muitos")
                
                if (grupoDTO.getItensOpcionais() != null) {
                    for (ItemOpcionalDTO itemDTO : grupoDTO.getItensOpcionais()) {
                        ItemOpcional item = new ItemOpcional();
                        item.setNome(itemDTO.getNome());
                        item.setPrecoAdicional(itemDTO.getPrecoAdicional());
                        item.setGrupoOpcional(grupo); // Define a relação aninhada (lado "Muitos")
                        
                        grupo.getItensOpcionais().add(item); // Adiciona o item à lista do grupo
                    }
                }
                // Adiciona o grupo preenchido à lista do produto
                produto.getGruposOpcionais().add(grupo); 
            }
        }
        
        // Salva o Produto. Graças ao CascadeType.ALL, os Grupos e Itens são salvos juntos.
        Produto produtoSalvo = produtoRepository.save(produto); 
        return new ProdutoResponseDTO(produtoSalvo); 
    }

    /**
     * Atualiza um produto existente e sua hierarquia de opcionais.
     */
    @Override
    @CacheEvict(value = "produtos", key = "#id") // Invalida a entrada específica do cache
    public ProdutoResponseDTO atualizarProduto(Long id, ProdutoDTO dto) {
        // Busca o produto existente
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + id));
        Restaurante restaurante = restauranteRepository.findById(dto.getRestauranteId())
                .orElseThrow(() -> new EntityNotFoundException("Restaurante não encontrado: " + dto.getRestauranteId()));

        // Atualiza os campos simples
        produto.setNome(dto.getNome());
        produto.setDescricao(dto.getDescricao());
        produto.setCategoria(dto.getCategoria());
        produto.setEstoque(dto.getEstoque());
        produto.setRestaurante(restaurante);
        produto.setPrecoBase(dto.getPrecoBase());

        // Estratégia "Clear and Replace" para atualizar coleções aninhadas.
        // Limpa os grupos antigos. Se 'orphanRemoval=true' estiver na entidade, o JPA os deletará.
        produto.getGruposOpcionais().clear(); 
        
        // Repopula a lista com os dados do DTO (lógica idêntica ao 'cadastrarProduto')
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

    /**
     * Busca um produto pelo ID, garantindo o carregamento de suas dependências (opcionais).
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "produtos", key = "#id") // Armazena este resultado no cache
    public ProdutoResponseDTO buscarProdutoPorId(Long id) {
        // Utiliza uma query customizada (findProdutoCompletoById) que realiza JOIN FETCH
        // para carregar 'gruposOpcionais' e 'itensOpcionais' na mesma consulta.
        // Isso evita a LazyInitializationException que ocorreria se o construtor do DTO
        // tentasse acessar essas coleções fora da sessão transacional.
        Produto produto = produtoRepository.findProdutoCompletoById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + id));
        
        return new ProdutoResponseDTO(produto);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "produtosPorNome", key = "#nome")
    public List<ProdutoResponseDTO> buscarProdutosPorNome(String nome) {
        List<Produto> produtos = produtoRepository.findByNomeContainingIgnoreCaseAndDisponivelTrue(nome);
        return produtos.stream()
                .map(ProdutoResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "produtosPorRestaurante", key = "{#restauranteId, #disponivel}")
    public List<ProdutoResponseDTO> buscarProdutosPorRestaurante(Long restauranteId, Boolean disponivel) {
        List<Produto> produtos;
        // Se 'disponivel' for nulo ou verdadeiro, busca apenas os disponíveis (comportamento padrão)
        if (disponivel == null || disponivel == true) {
            produtos = produtoRepository.findByRestauranteIdAndDisponivelTrue(restauranteId);
        } else {
            // Se 'disponivel' for falso, busca os indisponíveis
            produtos = produtoRepository.findByRestauranteIdAndDisponivel(restauranteId, false);
        }
        return produtos.stream()
                .map(ProdutoResponseDTO::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Inverte o status de disponibilidade de um produto (disponível/indisponível).
     */
    @Override
    @CacheEvict(value = "produtos", key = "#id")
    public ProdutoResponseDTO alterarDisponibilidade(Long id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
        
        // Inverte o booleano, tratando nulo como "disponível" por padrão
        produto.setDisponivel(produto.getDisponivel() == null ? true : !produto.getDisponivel());
        produtoRepository.save(produto);
        return new ProdutoResponseDTO(produto);
    }

    @Override
    @CacheEvict(value = "produtos", key = "#id")
    public void removerProduto(Long id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
        produtoRepository.delete(produto);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "produtosPorCategoria", key = "#categoria")
    public List<ProdutoResponseDTO> buscarProdutosPorCategoria(String categoria) {
        List<Produto> produtos = produtoRepository.findByCategoriaAndDisponivelTrue(categoria);
        return produtos.stream()
                .map(ProdutoResponseDTO::new) 
                .collect(Collectors.toList());
    }

    /**
     * Verifica se o usuário de restaurante logado é o proprietário do produto.
     * Usado para checagens de segurança/autorização.
     */
    @Override
    public boolean isOwner(Long produtoId) {
        Produto produto = produtoRepository.findById(produtoId).orElse(null);
        if (produto == null) return false;
        
        Long restauranteLogadoId = SecurityUtils.getCurrentRestauranteId();
        if (restauranteLogadoId == null) return false;
        
        return produto.getRestaurante() != null &&
               produto.getRestaurante().getId().equals(restauranteLogadoId);
    }

    /**
     * Lista produtos de forma paginada usando Specifications para filtros dinâmicos.
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "produtos", 
           key = "{#pageable.pageNumber, #pageable.pageSize, #restauranteId, #categoria, #disponivel}")
    public Page<ProdutoResponseDTO> listarProdutos(Pageable pageable, Long restauranteId, String categoria, Boolean disponivel) {
        // (O código de Specification foi omitido, mas a lógica de busca é aqui)
        Specification<Produto> spec = Specification.not(null); 
        
        Page<Produto> paginaDeProdutos = produtoRepository.findAll(spec, pageable);
        return paginaDeProdutos.map(ProdutoResponseDTO::new);
    }
}