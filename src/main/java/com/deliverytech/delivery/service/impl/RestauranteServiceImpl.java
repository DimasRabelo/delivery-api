package com.deliverytech.delivery.service.impl;

import com.deliverytech.delivery.dto.RestauranteDTO;
import com.deliverytech.delivery.dto.response.RestauranteResponseDTO;
import com.deliverytech.delivery.entity.Restaurante;
import com.deliverytech.delivery.exception.ConflictException;
import com.deliverytech.delivery.exception.EntityNotFoundException;
import com.deliverytech.delivery.repository.RestauranteRepository;
import com.deliverytech.delivery.service.RestauranteService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RestauranteServiceImpl implements RestauranteService {

    @Autowired
    private RestauranteRepository restauranteRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public RestauranteResponseDTO cadastrarRestaurante(RestauranteDTO dto) {
        restauranteRepository.findByNome(dto.getNome())
            .ifPresent(r -> { 
                throw new ConflictException(
                    "Restaurante já cadastrado: " + dto.getNome(),
                    "nome",
                    dto.getNome()
                ); 
            });

        Restaurante restaurante = modelMapper.map(dto, Restaurante.class);
        restaurante.setAtivo(true);
        validarDadosRestaurante(restaurante);

        Restaurante salvo = restauranteRepository.save(restaurante);
        return modelMapper.map(salvo, RestauranteResponseDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public RestauranteResponseDTO buscarRestaurantePorId(Long id) {
        Restaurante restaurante = restauranteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Restaurante não encontrado: " + id));
        return modelMapper.map(restaurante, RestauranteResponseDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestauranteResponseDTO> buscarRestaurantesPorCategoria(String categoria) {
        List<Restaurante> restaurantes = restauranteRepository.findByCategoria(categoria);
        return restaurantes.stream()
                .map(r -> modelMapper.map(r, RestauranteResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestauranteResponseDTO> buscarRestaurantesDisponiveis() {
        List<Restaurante> ativos = restauranteRepository.findByAtivoTrue();
        return ativos.stream()
                .map(r -> modelMapper.map(r, RestauranteResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public RestauranteResponseDTO atualizarRestaurante(Long id, RestauranteDTO dto) {
        Restaurante restaurante = restauranteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Restaurante não encontrado: " + id));

        restauranteRepository.findByNome(dto.getNome())
                .ifPresent(r -> { if (!r.getId().equals(id)) 
                    throw new ConflictException(
                        "Nome já cadastrado: " + dto.getNome(),
                        "nome",
                        dto.getNome()
                    ); 
                });

        restaurante.setNome(dto.getNome());
        restaurante.setCategoria(dto.getCategoria());
        restaurante.setEndereco(dto.getEndereco());
        restaurante.setTelefone(dto.getTelefone());
        restaurante.setTaxaEntrega(dto.getTaxaEntrega());
        restaurante.setAtivo(dto.getAtivo());

        validarDadosRestaurante(restaurante);
        Restaurante atualizado = restauranteRepository.save(restaurante);
        return modelMapper.map(atualizado, RestauranteResponseDTO.class);
    }

    @Override
    public RestauranteResponseDTO alterarStatusRestaurante(Long id) {
        Restaurante restaurante = restauranteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Restaurante não encontrado: " + id));

        restaurante.setAtivo(!restaurante.getAtivo());
        restauranteRepository.save(restaurante);
        return modelMapper.map(restaurante, RestauranteResponseDTO.class);
    }

    @Override
    public BigDecimal calcularTaxaEntrega(Long restauranteId, String cepDestino) {
        Restaurante restaurante = restauranteRepository.findById(restauranteId)
                .orElseThrow(() -> new EntityNotFoundException("Restaurante não encontrado: " + restauranteId));

        if (!restaurante.getAtivo()) {
            throw new ConflictException("Restaurante não está disponível");
        }

        BigDecimal taxaBase = restaurante.getTaxaEntrega() != null 
                ? restaurante.getTaxaEntrega() 
                : BigDecimal.valueOf(5.00);

        char ultimoDigito = cepDestino != null && !cepDestino.isEmpty()
                ? cepDestino.charAt(cepDestino.length() - 1)
                : '0';

        if (Character.isDigit(ultimoDigito) && (ultimoDigito - '0') % 2 != 0) {
            taxaBase = taxaBase.add(BigDecimal.valueOf(5.00));
        }

        return taxaBase.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public List<RestauranteResponseDTO> buscarRestaurantesProximos(String cep, Integer raioKm) {
        List<Restaurante> ativos = restauranteRepository.findByAtivoTrue();

        char ultimoDigito = cep != null && !cep.isEmpty() ? cep.charAt(cep.length() - 1) : '0';
        List<Restaurante> proximos;

        if (Character.isDigit(ultimoDigito) && (ultimoDigito - '0') % 2 != 0) {
            int meio = ativos.size() / 2;
            proximos = ativos.subList(0, meio);
        } else {
            proximos = ativos;
        }

        return proximos.stream()
                .map(r -> modelMapper.map(r, RestauranteResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public Page<RestauranteResponseDTO> listarRestaurantes(String categoria, Boolean ativo, Pageable pageable) {
        Page<Restaurante> restaurantesPage;

        if (categoria != null && ativo != null) {
            restaurantesPage = restauranteRepository.findByCategoriaAndAtivo(categoria, ativo, pageable);
        } else if (categoria != null) {
            restaurantesPage = restauranteRepository.findByCategoria(categoria, pageable);
        } else if (ativo != null) {
            restaurantesPage = restauranteRepository.findByAtivo(ativo, pageable);
        } else {
            restaurantesPage = restauranteRepository.findAll(pageable);
        }

        return restaurantesPage.map(r -> modelMapper.map(r, RestauranteResponseDTO.class));
    }

    private void validarDadosRestaurante(Restaurante restaurante) {
        if (restaurante.getNome() == null || restaurante.getNome().trim().isEmpty())
            throw new ConflictException("Nome é obrigatório", "nome", null);
        if (restaurante.getTelefone() == null || restaurante.getTelefone().trim().isEmpty())
            throw new ConflictException("Telefone é obrigatório", "telefone", null);
        if (restaurante.getEndereco() == null || restaurante.getEndereco().trim().isEmpty())
            throw new ConflictException("Endereço é obrigatório", "endereco", null);
        if (restaurante.getTaxaEntrega() != null && restaurante.getTaxaEntrega().compareTo(BigDecimal.ZERO) < 0)
            throw new ConflictException("Taxa de entrega não pode ser negativa", "taxaEntrega", restaurante.getTaxaEntrega());
    }
}
