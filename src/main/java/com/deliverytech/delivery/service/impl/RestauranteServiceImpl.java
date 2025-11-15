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

/**
 * Implementação do serviço de Restaurantes, contendo regras de negócio,
 * validações e a lógica de cálculo de taxa/proximidade baseada em CEP.
 */
@Service
@Transactional // Define que todos os métodos são transacionais por padrão (a menos que readOnly = true)
public class RestauranteServiceImpl implements RestauranteService {

    @Autowired
    private RestauranteRepository restauranteRepository;

    @Autowired
    private ModelMapper modelMapper; // Utilitário para mapear DTOs para Entidades e vice-versa

    /**
     * Cadastra um novo restaurante no sistema.
     */
    @Override
    public RestauranteResponseDTO cadastrarRestaurante(RestauranteDTO dto) {
        // Valida se já existe um restaurante com este nome (regra de unicidade)
        restauranteRepository.findByNome(dto.getNome())
            .ifPresent(r -> {
                throw new ConflictException("Restaurante já cadastrado: " + dto.getNome(), "nome", dto.getNome());
            });

        Restaurante restaurante = modelMapper.map(dto, Restaurante.class);
        restaurante.setAtivo(true); // Define o status inicial como ativo

        // Mapeia e anexa a entidade Endereco
        EnderecoDTO enderecoDTO = dto.getEndereco();
        Endereco endereco = modelMapper.map(enderecoDTO, Endereco.class);
        restaurante.setEndereco(endereco);
        
        validarDadosRestaurante(restaurante); // Valida campos obrigatórios

        Restaurante salvo = restauranteRepository.save(restaurante);
        return modelMapper.map(salvo, RestauranteResponseDTO.class);
    }

    /**
     * Atualiza um restaurante existente e seu endereço aninhado (se fornecido).
     */
    @Override
    public RestauranteResponseDTO atualizarRestaurante(Long id, RestauranteDTO dto) {
        Restaurante restaurante = restauranteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Restaurante não encontrado: " + id));

        // Valida unicidade do nome: permite o mesmo nome se for o próprio restaurante
        restauranteRepository.findByNome(dto.getNome())
                .ifPresent(r -> { if (!r.getId().equals(id))
                    throw new ConflictException("Nome já cadastrado: " + dto.getNome(), "nome", dto.getNome());
                });

        // Mapeia e atualiza os campos básicos
        restaurante.setNome(dto.getNome());
        restaurante.setCategoria(dto.getCategoria());
        // ... (Atualização dos outros campos básicos) ...

        // Atualiza a entidade Endereço aninhada
        if (dto.getEndereco() != null) {
            Endereco enderecoExistente = restaurante.getEndereco();
            if (enderecoExistente == null) {
                // Se o endereço ainda não existe, cria-o e associa ao restaurante
                enderecoExistente = new Endereco();
                restaurante.setEndereco(enderecoExistente);
            }
            // Atualiza os dados do endereço com os dados do DTO
            modelMapper.map(dto.getEndereco(), enderecoExistente);
        }

        validarDadosRestaurante(restaurante);
        Restaurante atualizado = restauranteRepository.save(restaurante);
        return modelMapper.map(atualizado, RestauranteResponseDTO.class);
    }
    

    /**
     * Calcula a taxa de entrega, simulando a distância com base na "área" (5 primeiros dígitos) do CEP.
     */
    @Override
    public BigDecimal calcularTaxaEntrega(Long restauranteId, String cepDestino) {
        Restaurante restaurante = restauranteRepository.findById(restauranteId)
                .orElseThrow(() -> new EntityNotFoundException("Restaurante não encontrado: " + restauranteId));

        if (restaurante.getAtivo() == null || !restaurante.getAtivo()) {
            throw new ConflictException("Restaurante não está disponível");
        }

        // 1. Obtém e limpa o CEP do restaurante
        if (restaurante.getEndereco() == null || restaurante.getEndereco().getCep() == null) {
             throw new BusinessException("Restaurante está com endereço incompleto.");
        }
        String cepRestaurante = restaurante.getEndereco().getCep().replaceAll("[^0-9]", "");
        
        // 2. Obtém e limpa o CEP do destino
        String cepCliente = cepDestino.replaceAll("[^0-9]", "");
        
        // Validação simples
        if (cepRestaurante.length() < 8 || cepCliente.length() < 8) {
             throw new BusinessException("CEP inválido para cálculo de taxa.");
        }

        // 3. Pega a "Área" (primeiros 5 dígitos) de cada CEP para simular proximidade
        String areaRestaurante = cepRestaurante.substring(0, 5);
        String areaCliente = cepCliente.substring(0, 5);
        
        BigDecimal taxaFinal;
        
        // 4. Lógica de Simulação de Distância
        BigDecimal taxaBase = restaurante.getTaxaEntrega() != null
                ? restaurante.getTaxaEntrega()
                : BigDecimal.valueOf(5.00); // Taxa padrão se não for definida

        if (areaRestaurante.equals(areaCliente)) {
            // Mesma área de CEP: usa a taxa base
            taxaFinal = taxaBase;
        } else {
            // Áreas diferentes: adiciona R$ 5,00 à taxa base (simulando maior distância)
            taxaFinal = taxaBase.add(BigDecimal.valueOf(5.00));
        }

        // Retorna o valor com duas casas decimais
        return taxaFinal.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Busca restaurantes próximos, simulando "proximidade" ao comparar a "área" (5 primeiros dígitos) do CEP.
     * O parâmetro 'raioKm' é ignorado, pois o cálculo é baseado apenas no CEP.
     */
    @Override
    public List<RestauranteResponseDTO> buscarRestaurantesProximos(String cep, Integer raioKm) {
        // 1. Obtém a "Área" do CEP do cliente
        String cepCliente = cep.replaceAll("[^0-9]", "");
        if (cepCliente.length() < 8) {
            // Se o CEP for inválido, retorna todos os restaurantes ativos
            return buscarRestaurantesDisponiveis();
        }
        String areaCliente = cepCliente.substring(0, 5);
        
        // 2. Busca todos os restaurantes ativos
        List<Restaurante> ativos = restauranteRepository.findByAtivoTrue();

        // 3. Filtra os restaurantes que estão na mesma "Área" de CEP
        List<Restaurante> proximos = ativos.stream()
            .filter(restaurante -> {
                if (restaurante.getEndereco() != null && restaurante.getEndereco().getCep() != null) {
                    String cepRestaurante = restaurante.getEndereco().getCep().replaceAll("[^0-9]", "");
                    if (cepRestaurante.length() >= 5) {
                        String areaRestaurante = cepRestaurante.substring(0, 5);
                        return areaRestaurante.equals(areaCliente); // Filtro de proximidade
                    }
                }
                return false; // Ignora restaurantes sem CEP válido
            })
            .collect(Collectors.toList());

        // 4. Mapeia e retorna
        return proximos.stream()
                .map(r -> modelMapper.map(r, RestauranteResponseDTO.class))
                .collect(Collectors.toList());
    }
    
    // ... (Outros métodos CRUD simples: buscarPorId, buscarPorCategoria, buscarDisponiveis) ...

    /**
     * Inverte o status de ativação de um restaurante (ativo/inativo).
     */
    @Override
    public RestauranteResponseDTO alterarStatusRestaurante(Long id) {
        Restaurante restaurante = restauranteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Restaurante não encontrado: " + id));
        // Inverte o status
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
        
        // Lógica de busca condicional usando métodos de query derivation do Spring Data JPA
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
        // Checagens de nulidade e formato
        if (restaurante.getNome() == null || restaurante.getNome().trim().isEmpty())
            throw new ConflictException("Nome é obrigatório", "nome", null);
        // ... (Outras checagens de campos) ...
        if (restaurante.getEndereco() == null)
            throw new ConflictException("Endereço é obrigatório", "endereco", null);
        // Regra de negócio: Taxa de entrega não pode ser negativa
        if (restaurante.getTaxaEntrega() != null && restaurante.getTaxaEntrega().compareTo(BigDecimal.ZERO) < 0)
            throw new ConflictException("Taxa de entrega não pode ser negativa", "taxaEntrega", restaurante.getTaxaEntrega());
    }
    
    // Métodos simples (buscarRestaurantePorId, buscarRestaurantesPorCategoria, buscarRestaurantesDisponiveis)
    // foram omitidos na explicação detalhada acima para focar na lógica mais complexa.

    // Implementação dos métodos restantes para que o código esteja completo:
    
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
}