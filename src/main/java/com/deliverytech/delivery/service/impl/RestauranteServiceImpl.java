package com.deliverytech.delivery.service.impl;

import com.deliverytech.delivery.dto.RestauranteDTO;
import com.deliverytech.delivery.dto.EnderecoDTO; // IMPORT ADICIONADO
import com.deliverytech.delivery.dto.response.RestauranteResponseDTO;
import com.deliverytech.delivery.entity.Restaurante;
import com.deliverytech.delivery.entity.Endereco; // IMPORT ADICIONADO
import com.deliverytech.delivery.exception.ConflictException;
import com.deliverytech.delivery.exception.EntityNotFoundException;
import com.deliverytech.delivery.repository.RestauranteRepository;
//import com.deliverytech.delivery.repository.EnderecoRepository; 
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

   // @Autowired
   // private EnderecoRepository enderecoRepository; // <-- ADICIONADO

    @Autowired
    private ModelMapper modelMapper;

    /**
     * Cadastra um novo restaurante (VERSÃO REFATORADA).
     * Agora, também cria e associa a entidade Endereco.
     */
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

        // 1. Mapeia os campos simples do Restaurante (nome, categoria, etc.)
        Restaurante restaurante = modelMapper.map(dto, Restaurante.class);
        restaurante.setAtivo(true); // Define como ativo por padrão

        // 2. Mapeia o EnderecoDTO aninhado para a Entidade Endereco
        // (Assume que o RestauranteDTO refatorado tem 'getEndereco()' que retorna um EnderecoDTO)
        EnderecoDTO enderecoDTO = dto.getEndereco();
        Endereco endereco = modelMapper.map(enderecoDTO, Endereco.class);

        // 3. Associa o Endereço ao Restaurante
        // (Isso funciona por causa do @OneToOne(cascade = CascadeType.ALL) na entidade Restaurante)
        restaurante.setEndereco(endereco);

        validarDadosRestaurante(restaurante); // Valida os dados antes de salvar

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

    /**
     * Atualiza um restaurante (VERSÃO REFATORADA).
     * Agora, atualiza os campos do Endereço associado.
     */
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

        // 1. Atualiza os campos simples do Restaurante (copia do DTO para a Entidade)
        // (O ModelMapper poderia fazer isso, mas setar manualmente é mais seguro
        //  para não sobrescrever o ID ou os relacionamentos por engano)
        restaurante.setNome(dto.getNome());
        restaurante.setCategoria(dto.getCategoria());
        restaurante.setTelefone(dto.getTelefone());
        restaurante.setTaxaEntrega(dto.getTaxaEntrega());
        restaurante.setAtivo(dto.getAtivo());
        restaurante.setTempoEntrega(dto.getTempoEntrega()); // (Estava faltando no seu código antigo)
        restaurante.setHorarioFuncionamento(dto.getHorarioFuncionamento()); // (Estava faltando)
        restaurante.setAvaliacao(dto.getAvaliacao()); // (Estava faltando)

        // 2. CORREÇÃO: Atualiza a entidade Endereço existente
        if (dto.getEndereco() != null) {
            Endereco enderecoExistente = restaurante.getEndereco();
            if (enderecoExistente == null) {
                // Caso raro: restaurante antigo não tinha endereço
                enderecoExistente = new Endereco();
                restaurante.setEndereco(enderecoExistente);
            }
            // Mapeia os campos do DTO (dto.getEndereco()) para a Entidade (enderecoExistente)
            modelMapper.map(dto.getEndereco(), enderecoExistente);
        }

        validarDadosRestaurante(restaurante);
        Restaurante atualizado = restauranteRepository.save(restaurante);
        return modelMapper.map(atualizado, RestauranteResponseDTO.class);
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
    public BigDecimal calcularTaxaEntrega(Long restauranteId, String cepDestino) {
        // Esta lógica de placeholder está OK por enquanto.
        // TODO: A lógica real deveria usar o Endereco do restaurante e o Endereco do cliente
        // para calcular a distância (via latitude/longitude) e aplicar uma taxa.
        Restaurante restaurante = restauranteRepository.findById(restauranteId)
                .orElseThrow(() -> new EntityNotFoundException("Restaurante não encontrado: " + restauranteId));

        if (restaurante.getAtivo() == null || !restaurante.getAtivo()) {
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
        // TODO: ESTA LÓGICA PRECISA SER REFEITA.
        // A lógica de "último dígito do CEP" era um placeholder.
        // A lógica real deve:
        // 1. Chamar uma API (ex: Google Geocoding) para converter o 'cep' em 'userLat' e 'userLng'.
        // 2. Chamar 'restauranteRepository.findByAtivoTrue()'.
        // 3. Iterar na lista de restaurantes.
        // 4. Para cada 'restaurante', pegar 'restaurante.getEndereco().getLatitude()' e '.getLongitude()'.
        // 5. Usar a fórmula de Haversine para calcular a distância.
        // 6. Retornar apenas os restaurantes onde a 'distancia <= raioKm'.
        
        // Por enquanto, apenas retornamos os ativos para o código compilar:
        List<Restaurante> ativos = restauranteRepository.findByAtivoTrue();
        return ativos.stream()
                .map(r -> modelMapper.map(r, RestauranteResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public Page<RestauranteResponseDTO> listarRestaurantes(String categoria, Boolean ativo, Pageable pageable) {
        // Esta lógica está OK
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

    /**
     * Valida os dados do restaurante (VERSÃO REFATORADA).
     */
    private void validarDadosRestaurante(Restaurante restaurante) {
        if (restaurante.getNome() == null || restaurante.getNome().trim().isEmpty())
            throw new ConflictException("Nome é obrigatório", "nome", null);
        if (restaurante.getTelefone() == null || restaurante.getTelefone().trim().isEmpty())
            throw new ConflictException("Telefone é obrigatório", "telefone", null);
        
        // --- CORREÇÃO (GARGALO 1) ---
        // A validação agora checa se o OBJETO Endereco existe
        if (restaurante.getEndereco() == null)
            throw new ConflictException("Endereço é obrigatório", "endereco", null);
        // As validações internas (rua, cep) são feitas pelo @Valid no DTO.

        if (restaurante.getTaxaEntrega() != null && restaurante.getTaxaEntrega().compareTo(BigDecimal.ZERO) < 0)
            throw new ConflictException("Taxa de entrega não pode ser negativa", "taxaEntrega", restaurante.getTaxaEntrega());
    }
}