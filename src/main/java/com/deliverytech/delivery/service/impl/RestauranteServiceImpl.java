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
     * Cadastra um novo restaurante no sistema.
     */
    @Override
    public RestauranteResponseDTO cadastrarRestaurante(RestauranteDTO dto) {
        // Valida se já existe um restaurante com este nome
        restauranteRepository.findByNome(dto.getNome())
            .ifPresent(r -> {
                throw new ConflictException("Restaurante já cadastrado: " + dto.getNome(), "nome", dto.getNome());
            });

        Restaurante restaurante = modelMapper.map(dto, Restaurante.class);
        restaurante.setAtivo(true); // Padrão ao cadastrar

        // Mapeia o DTO de endereço para a entidade aninhada
        EnderecoDTO enderecoDTO = dto.getEndereco();
        Endereco endereco = modelMapper.map(enderecoDTO, Endereco.class);
        restaurante.setEndereco(endereco);
        
        validarDadosRestaurante(restaurante);

        Restaurante salvo = restauranteRepository.save(restaurante);
        return modelMapper.map(salvo, RestauranteResponseDTO.class);
    }

    /**
     * Atualiza um restaurante existente e seu endereço aninhado.
     */
    @Override
    public RestauranteResponseDTO atualizarRestaurante(Long id, RestauranteDTO dto) {
        Restaurante restaurante = restauranteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Restaurante não encontrado: " + id));

        // Valida se o novo nome já está em uso por OUTRO restaurante
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
                // Cria um novo endereço se o restaurante não tinha um
                enderecoExistente = new Endereco();
                restaurante.setEndereco(enderecoExistente);
            }
            // Atualiza os dados do endereço existente com os dados do DTO
            modelMapper.map(dto.getEndereco(), enderecoExistente);
        }

        validarDadosRestaurante(restaurante);
        Restaurante atualizado = restauranteRepository.save(restaurante);
        return modelMapper.map(atualizado, RestauranteResponseDTO.class);
    }
    

    /**
     * Calcula a taxa de entrega, simulando a distância com base na "área"
     * (5 primeiros dígitos) do CEP.
     */
    @Override
    public BigDecimal calcularTaxaEntrega(Long restauranteId, String cepDestino) {
        Restaurante restaurante = restauranteRepository.findById(restauranteId)
                .orElseThrow(() -> new EntityNotFoundException("Restaurante não encontrado: " + restauranteId));

        if (restaurante.getAtivo() == null || !restaurante.getAtivo()) {
            throw new ConflictException("Restaurante não está disponível");
        }

        // --- LÓGICA DE TAXA COM BASE NO CEP ---
        
        // 1. Pega o CEP do restaurante (da entidade Endereco)
        if (restaurante.getEndereco() == null || restaurante.getEndereco().getCep() == null) {
             throw new BusinessException("Restaurante está com endereço incompleto.");
        }
        String cepRestaurante = restaurante.getEndereco().getCep().replaceAll("[^0-9]", "");
        
        // 2. Limpa o CEP do destino
        String cepCliente = cepDestino.replaceAll("[^0-9]", "");
        
        // 3. Validação simples de formato
        if (cepRestaurante.length() < 8 || cepCliente.length() < 8) {
             throw new BusinessException("CEP inválido para cálculo de taxa.");
        }

        // 4. Pega a "Área" (primeiros 5 dígitos) de cada CEP
        String areaRestaurante = cepRestaurante.substring(0, 5);
        String areaCliente = cepCliente.substring(0, 5);
        
        BigDecimal taxaFinal;
        
        // 5. Compara as áreas
        if (areaRestaurante.equals(areaCliente)) {
            // Se for na mesma área, usa a taxa base (ou um padrão)
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
     * Busca restaurantes próximos, simulando "proximidade" ao comparar a "área"
     * (5 primeiros dígitos) do CEP do cliente com o do restaurante.
     */
    @Override
    public List<RestauranteResponseDTO> buscarRestaurantesProximos(String cep, Integer raioKm) {
        // 1. Limpa o CEP do cliente
        String cepCliente = cep.replaceAll("[^0-9]", "");
        if (cepCliente.length() < 8) {
            // Se o CEP for inválido, retorna todos os restaurantes disponíveis
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
                        // O parâmetro 'raioKm' é ignorado nesta lógica,
                        // a "proximidade" é definida apenas pela área do CEP.
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
    
    /**
     * Busca um restaurante específico pelo seu ID.
     */
    @Override
    @Transactional(readOnly = true)
    public RestauranteResponseDTO buscarRestaurantePorId(Long id) {
        Restaurante restaurante = restauranteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Restaurante não encontrado: " + id));
        return modelMapper.map(restaurante, RestauranteResponseDTO.class);
    }
    
    /**
     * Busca restaurantes por categoria.
     */
    @Override
    @Transactional(readOnly = true)
    public List<RestauranteResponseDTO> buscarRestaurantesPorCategoria(String categoria) {
        List<Restaurante> restaurantes = restauranteRepository.findByCategoria(categoria);
        return restaurantes.stream()
                .map(r -> modelMapper.map(r, RestauranteResponseDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * Retorna uma lista de todos os restaurantes marcados como "ativos".
     */
    @Override
    @Transactional(readOnly = true)
    public List<RestauranteResponseDTO> buscarRestaurantesDisponiveis() {
        List<Restaurante> ativos = restauranteRepository.findByAtivoTrue();
        return ativos.stream()
                .map(r -> modelMapper.map(r, RestauranteResponseDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * Inverte o status de ativação de um restaurante (ativo/inativo).
     */
    @Override
    public RestauranteResponseDTO alterarStatusRestaurante(Long id) {
        Restaurante restaurante = restauranteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Restaurante não encontrado: " + id));
        // Inverte o status, tratando nulo como "ativo" (torna-se inativo)
        restaurante.setAtivo(restaurante.getAtivo() == null ? true : !restaurante.getAtivo());
        restauranteRepository.save(restaurante);
        return modelMapper.map(restaurante, RestauranteResponseDTO.class);
    }
    
    /**
     * Lista restaurantes de forma paginada, com filtros opcionais.
     */
    @Override
    public Page<RestauranteResponseDTO> listarRestaurantes(String categoria, Boolean ativo, Pageable pageable) {
        Page<Restaurante> restaurantesPage;
        
        // Lógica de busca com base nos filtros fornecidos
        if (categoria != null && ativo != null) {
            restaurantesPage = restauranteRepository.findByCategoriaAndAtivo(categoria, ativo, pageable);
        } else if (categoria != null) {
            restaurantesPage = restauranteRepository.findByCategoria(categoria, pageable);
        } else if (ativo != null) {
            restaurantesPage = restauranteRepository.findByAtivo(ativo, pageable);
        } else {
            restaurantesPage = restauranteRepository.findAll(pageable);
        }
        
        // Mapeia a página de entidades para uma página de DTOs
        return restaurantesPage.map(r -> modelMapper.map(r, RestauranteResponseDTO.class));
    }

    /**
     * Método utilitário para validar os campos obrigatórios e regras de negócio
     * de uma entidade Restaurante antes de salvar.
     */
    private void validarDadosRestaurante(Restaurante restaurante) {
        if (restaurante.getNome() == null || restaurante.getNome().trim().isEmpty())
            throw new ConflictException("Nome é obrigatório", "nome", null);
        if (restaurante.getTelefone() == null || restaurante.getTelefone().trim().isEmpty())
            throw new ConflictException("Telefone é obrigatório", "telefone", null);
        if (restaurante.getEndereco() == null)
            throw new ConflictException("Endereço é obrigatório", "endereco", null);
        if (restaurante.getTaxaEntrega() != null && restaurante.getTaxaEntrega().compareTo(BigDecimal.ZERO) < 0)
            throw new ConflictException("Taxa de entrega não pode ser negativa", "taxaEntrega", restaurante.getTaxaEntrega());
    }
}