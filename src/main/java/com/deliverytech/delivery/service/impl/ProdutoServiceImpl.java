package com.deliverytech.delivery.service.impl;

import com.deliverytech.delivery.dto.ProdutoDTO;
import com.deliverytech.delivery.dto.ProdutoResponseDTO;
import com.deliverytech.delivery.entity.Produto;
import com.deliverytech.delivery.entity.Restaurante;
import com.deliverytech.delivery.exception.BusinessException;
import com.deliverytech.delivery.exception.EntityNotFoundException;
import com.deliverytech.delivery.repository.ProdutoRepository;
import com.deliverytech.delivery.repository.RestauranteRepository;
import com.deliverytech.delivery.service.ProdutoService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProdutoServiceImpl implements ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private RestauranteRepository restauranteRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public ProdutoResponseDTO cadastrarProduto(ProdutoDTO dto) {
        Restaurante restaurante = restauranteRepository.findById(dto.getRestauranteId())
                .orElseThrow(() -> new EntityNotFoundException("Restaurante não encontrado: " + dto.getRestauranteId()));

        if (!restaurante.getAtivo()) {
            throw new BusinessException("Não é possível adicionar produto em restaurante inativo");
        }

        if (produtoRepository.existsByNomeAndRestauranteId(dto.getNome(), dto.getRestauranteId())) {
            throw new BusinessException("Já existe um produto com esse nome neste restaurante");
        }

        Produto produto = modelMapper.map(dto, Produto.class);
        produto.setDisponivel(dto.getDisponivel() != null ? dto.getDisponivel() : true);
        produto.setRestaurante(restaurante);

        Produto salvo = produtoRepository.save(produto);

        ProdutoResponseDTO responseDTO = modelMapper.map(salvo, ProdutoResponseDTO.class);
        responseDTO.setRestauranteId(salvo.getRestaurante().getId()); // Map manual
        return responseDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public ProdutoResponseDTO buscarProdutoPorId(Long id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + id));

        if (!produto.getDisponivel()) {
            throw new BusinessException("Produto não está disponível");
        }

        ProdutoResponseDTO responseDTO = modelMapper.map(produto, ProdutoResponseDTO.class);
        responseDTO.setRestauranteId(produto.getRestaurante().getId()); // Map manual
        return responseDTO;
    }

    @Override
@Transactional(readOnly = true)
public List<ProdutoResponseDTO> buscarProdutosPorNome(String nome) {
   List<Produto> produtos = produtoRepository.findByNomeContainingIgnoreCaseAndDisponivelTrue(nome);

    return produtos.stream()
            .map(p -> {
                ProdutoResponseDTO dto = modelMapper.map(p, ProdutoResponseDTO.class);
                dto.setRestauranteId(p.getRestaurante().getId());
                return dto;
            })
            .collect(Collectors.toList());
}

    @Override
    @Transactional(readOnly = true)
    public List<ProdutoResponseDTO> buscarProdutosPorRestaurante(Long restauranteId) {
        List<Produto> produtos = produtoRepository.findByRestauranteIdAndDisponivelTrue(restauranteId);
        return produtos.stream()
                .map(p -> {
                    ProdutoResponseDTO dto = modelMapper.map(p, ProdutoResponseDTO.class);
                    dto.setRestauranteId(p.getRestaurante().getId()); // Map manual
                    return dto;
                })
                .collect(Collectors.toList());
    }

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

        Produto atualizado = produtoRepository.save(produto);

        ProdutoResponseDTO responseDTO = modelMapper.map(atualizado, ProdutoResponseDTO.class);
        responseDTO.setRestauranteId(atualizado.getRestaurante().getId()); // Map manual
        return responseDTO;
    }

    @Override
    public ProdutoResponseDTO alterarDisponibilidade(Long id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));

        // Alterna o valor atual da disponibilidade
        produto.setDisponivel(!produto.getDisponivel());
        produtoRepository.save(produto);

        return new ProdutoResponseDTO(produto);
    }

    @Override
public void removerProduto(Long id) {
    Produto produto = produtoRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + id));

    // Se houver regra de negócio para impedir exclusão
    // if (pedidoRepository.existsByProdutosContaining(produto)) {
    //     throw new BusinessException("Produto possui pedidos associados");
    // }

    produtoRepository.delete(produto);
}

    @Override
    @Transactional(readOnly = true)
    public List<ProdutoResponseDTO> buscarProdutosPorCategoria(String categoria) {
        List<Produto> produtos = produtoRepository.findByCategoria(categoria);
        return produtos.stream()
                .map(p -> {
                    ProdutoResponseDTO dto = modelMapper.map(p, ProdutoResponseDTO.class);
                    dto.setRestauranteId(p.getRestaurante().getId()); // Map manual
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
