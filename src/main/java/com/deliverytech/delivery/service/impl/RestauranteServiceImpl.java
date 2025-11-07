package com.deliverytech.delivery.service.impl;

import com.deliverytech.delivery.dto.request.EnderecoDTO;
import com.deliverytech.delivery.dto.request.RestauranteDTO;
import com.deliverytech.delivery.dto.response.RestauranteResponseDTO;
import com.deliverytech.delivery.entity.Restaurante;
import com.deliverytech.delivery.entity.Endereco;
import com.deliverytech.delivery.exception.BusinessException;
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

    /**
     * Cadastra um novo restaurante (VERSÃO REFATORADA).
     * (Este método está OK como na mensagem 101)
     */
    @Override
    public RestauranteResponseDTO cadastrarRestaurante(RestauranteDTO dto) {
        restauranteRepository.findByNome(dto.getNome())
            .ifPresent(r -> { 
                throw new ConflictException("Restaurante já cadastrado: " + dto.getNome(), "nome", dto.getNome()); 
            });

        Restaurante restaurante = modelMapper.map(dto, Restaurante.class);
        restaurante.setAtivo(true);

        EnderecoDTO enderecoDTO = dto.getEndereco();
        Endereco endereco = modelMapper.map(enderecoDTO, Endereco.class);
        restaurante.setEndereco(endereco);
        
        validarDadosRestaurante(restaurante);

        Restaurante salvo = restauranteRepository.save(restaurante);
        return modelMapper.map(salvo, RestauranteResponseDTO.class);
    }

    /**
     * Atualiza um restaurante (VERSÃO REFATORADA).
     * (Este método está OK como na mensagem 101)
     */
    @Override
    public RestauranteResponseDTO atualizarRestaurante(Long id, RestauranteDTO dto) {
        Restaurante restaurante = restauranteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Restaurante não encontrado: " + id));

        restauranteRepository.findByNome(dto.getNome())
                .ifPresent(r -> { if (!r.getId().equals(id)) 
                    throw new ConflictException("Nome já cadastrado: " + dto.getNome(), "nome", dto.getNome()); 
                });

        // Mapeia os campos simples
        restaurante.setNome(dto.getNome());
        restaurante.setCategoria(dto.getCategoria());
        restaurante.setTelefone(dto.getTelefone());
        restaurante.setTaxaEntrega(dto.getTaxaEntrega());
        restaurante.setAtivo(dto.getAtivo());
        restaurante.setTempoEntrega(dto.getTempoEntrega());
        restaurante.setHorarioFuncionamento(dto.getHorarioFuncionamento());

        // Atualiza a entidade Endereço aninhada
        if (dto.getEndereco() != null) {
            Endereco enderecoExistente = restaurante.getEndereco();
            if (enderecoExistente == null) {
                enderecoExistente = new Endereco();
                restaurante.setEndereco(enderecoExistente);
            }
            modelMapper.map(dto.getEndereco(), enderecoExistente);
        }

        validarDadosRestaurante(restaurante);
        Restaurante atualizado = restauranteRepository.save(restaurante);
        return modelMapper.map(atualizado, RestauranteResponseDTO.class);
    }
    

    /**
     * Calcula a taxa de entrega (VERSÃO REFATORADA COM LÓGICA DE CEP).
     * Simula a distância comparando os 5 primeiros dígitos do CEP.
     */
    @Override
    public BigDecimal calcularTaxaEntrega(Long restauranteId, String cepDestino) {
        Restaurante restaurante = restauranteRepository.findById(restauranteId)
                .orElseThrow(() -> new EntityNotFoundException("Restaurante não encontrado: " + restauranteId));

        if (restaurante.getAtivo() == null || !restaurante.getAtivo()) {
            throw new ConflictException("Restaurante não está disponível");
        }

        // --- LÓGICA DE TAXA REFATORADA COM BASE NO CEP ---
        
        // 1. Pega o CEP do restaurante (da nova entidade Endereco)
        if (restaurante.getEndereco() == null || restaurante.getEndereco().getCep() == null) {
             throw new BusinessException("Restaurante está com endereço incompleto.");
        }
        String cepRestaurante = restaurante.getEndereco().getCep().replaceAll("[^0-9]", "");
        
        // 2. Limpa o CEP do destino
        String cepCliente = cepDestino.replaceAll("[^0-9]", "");
        
        // 3. Validação simples
        if (cepRestaurante.length() < 8 || cepCliente.length() < 8) {
             throw new BusinessException("CEP inválido para cálculo de taxa.");
        }

        // 4. Pega a "Área" (primeiros 5 dígitos) de cada CEP
        String areaRestaurante = cepRestaurante.substring(0, 5);
        String areaCliente = cepCliente.substring(0, 5);
        
        BigDecimal taxaFinal;
        
        // 5. Compara as áreas
        if (areaRestaurante.equals(areaCliente)) {
            // Se for na mesma área, usa a taxa base (ou R$ 5,00)
            taxaFinal = restaurante.getTaxaEntrega() != null 
                ? restaurante.getTaxaEntrega() 
                : BigDecimal.valueOf(5.00);
        } else {
            // Se for em área diferente, adiciona R$ 5,00 (simulando distância)
            BigDecimal taxaBase = restaurante.getTaxaEntrega() != null 
                ? restaurante.getTaxaEntrega() 
                : BigDecimal.valueOf(5.00);
            taxaFinal = taxaBase.add(BigDecimal.valueOf(5.00));
        }

        return taxaFinal.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Busca restaurantes próximos (VERSÃO REFATORADA COM LÓGICA DE CEP).
     * Simula a "proximidade" comparando os 5 primeiros dígitos do CEP.
     */
    @Override
    public List<RestauranteResponseDTO> buscarRestaurantesProximos(String cep, Integer raioKm) {
        // 1. Limpa o CEP do cliente
        String cepCliente = cep.replaceAll("[^0-9]", "");
        if (cepCliente.length() < 8) {
            // Se o CEP for inválido, retorna todos os restaurantes
            return buscarRestaurantesDisponiveis();
        }
        String areaCliente = cepCliente.substring(0, 5);
        
        // 2. Busca todos os restaurantes ativos
        List<Restaurante> ativos = restauranteRepository.findByAtivoTrue();

        // 3. Filtra os restaurantes
        List<Restaurante> proximos = ativos.stream()
            .filter(restaurante -> {
                // 4. Pega o CEP do restaurante e compara a "área"
                if (restaurante.getEndereco() != null && restaurante.getEndereco().getCep() != null) {
                    String cepRestaurante = restaurante.getEndereco().getCep().replaceAll("[^0-9]", "");
                    if (cepRestaurante.length() >= 5) {
                        String areaRestaurante = cepRestaurante.substring(0, 5);
                        // (Aqui, o 'raioKm' é ignorado, mas a lógica de área funciona)
                        return areaRestaurante.equals(areaCliente); 
                    }
                }
                return false; // Ignora restaurantes sem CEP
            })
            .collect(Collectors.toList());

        return proximos.stream()
                .map(r -> modelMapper.map(r, RestauranteResponseDTO.class))
                .collect(Collectors.toList());
    }
    

    // ==========================================================
    // (O resto dos seus métodos: buscarPorId, alterarStatus, etc.)
    // (Eles não precisam de mudanças e estão OK)
    // ==========================================================

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
    public RestauranteResponseDTO alterarStatusRestaurante(Long id) {
        Restaurante restaurante = restauranteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Restaurante não encontrado: " + id));
        restaurante.setAtivo(restaurante.getAtivo() == null ? true : !restaurante.getAtivo());
        restauranteRepository.save(restaurante);
        return modelMapper.map(restaurante, RestauranteResponseDTO.class);
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
        if (restaurante.getEndereco() == null) // (Validação Refatorada)
            throw new ConflictException("Endereço é obrigatório", "endereco", null);
        if (restaurante.getTaxaEntrega() != null && restaurante.getTaxaEntrega().compareTo(BigDecimal.ZERO) < 0)
            throw new ConflictException("Taxa de entrega não pode ser negativa", "taxaEntrega", restaurante.getTaxaEntrega());
    }
}