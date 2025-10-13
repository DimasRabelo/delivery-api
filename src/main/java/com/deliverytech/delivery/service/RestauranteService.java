package com.deliverytech.delivery.service;

import com.deliverytech.delivery.entity.Restaurante;
import com.deliverytech.delivery.repository.RestauranteRepository;
import com.deliverytech.delivery.dto.RelatorioVendas; // importando a interface de projeção
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RestauranteService {

    @Autowired
    private RestauranteRepository restauranteRepository;

    /**
     * Cadastrar novo restaurante
     */
    public Restaurante cadastrar(Restaurante restaurante) {
        // Validar nome único
        if (restauranteRepository.findByNome(restaurante.getNome()).isPresent()) {
            throw new IllegalArgumentException("Restaurante já cadastrado: " + restaurante.getNome());
        }

        validarDadosRestaurante(restaurante);
        restaurante.setAtivo(true);

        return restauranteRepository.save(restaurante);
    }

    /**
     * Buscar por ID
     */
    @Transactional(readOnly = true)
    public Optional<Restaurante> buscarPorId(Long id) {
        return restauranteRepository.findById(id);
    }

    /**
     * Listar restaurantes ativos
     */
    @Transactional(readOnly = true)
    public List<Restaurante> listarAtivos() {
        return restauranteRepository.findByAtivoTrue();
    }

    /**
     * Buscar por categoria
     */
    @Transactional(readOnly = true)
    public List<Restaurante> buscarPorCategoria(String categoria) {
        return restauranteRepository.findByCategoria(categoria);
    }

    /**
     * Atualizar restaurante
     */
    public Restaurante atualizar(Long id, Restaurante restauranteAtualizado) {
        Restaurante restaurante = buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Restaurante não encontrado: " + id));

        // Verificar nome único (se mudou)
        if (!restaurante.getNome().equals(restauranteAtualizado.getNome())
                && restauranteRepository.findByNome(restauranteAtualizado.getNome()).isPresent()) {
            throw new IllegalArgumentException("Nome já cadastrado: " + restauranteAtualizado.getNome());
        }

        restaurante.setNome(restauranteAtualizado.getNome());
        restaurante.setCategoria(restauranteAtualizado.getCategoria());
        restaurante.setEndereco(restauranteAtualizado.getEndereco());
        restaurante.setTelefone(restauranteAtualizado.getTelefone());
        restaurante.setTaxaEntrega(restauranteAtualizado.getTaxaEntrega());

        return restauranteRepository.save(restaurante);
    }

    /**
     * Inativar restaurante
     */
    public void inativar(Long id) {
        Restaurante restaurante = buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Restaurante não encontrado: " + id));

        restaurante.setAtivo(false);
        restauranteRepository.save(restaurante);
    }

    /**
     * Validar dados do restaurante
     */
    private void validarDadosRestaurante(Restaurante restaurante) {
        if (restaurante.getNome() == null || restaurante.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }

        if (restaurante.getTaxaEntrega() != null && restaurante.getTaxaEntrega().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Taxa de entrega não pode ser negativa");
        }
    }

    /**
     * Gerar relatório de vendas por restaurante (projeção)
     */
    @Transactional(readOnly = true)
    public List<RelatorioVendas> gerarRelatorioVendas() {
        return restauranteRepository.relatorioVendasPorRestaurante();
    }
}
