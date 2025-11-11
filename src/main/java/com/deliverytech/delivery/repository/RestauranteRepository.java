package com.deliverytech.delivery.repository;

import com.deliverytech.delivery.entity.Restaurante;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repositório Spring Data JPA para a entidade {@link Restaurante}.
 * Gerencia operações CRUD e consultas por nome, categoria, avaliação, taxa de entrega,
 * e consultas customizadas para produtos ativos e categorias disponíveis.
 */
@Repository
public interface RestauranteRepository extends JpaRepository<Restaurante, Long> {

    // =================== CONSULTAS BÁSICAS ===================

    /** Busca um restaurante pelo nome exato */
    Optional<Restaurante> findByNome(String nome);

    /** Busca todos os restaurantes ativos */
    List<Restaurante> findByAtivoTrue();

    /** Busca restaurantes por categoria (todos) */
    List<Restaurante> findByCategoria(String categoria);

    /** Busca restaurantes cujo nome contenha a string informada (case insensitive), apenas ativos */
    List<Restaurante> findByNomeContainingIgnoreCaseAndAtivoTrue(String nome);

    /** Busca restaurantes com avaliação mínima informada, apenas ativos */
    List<Restaurante> findByAvaliacaoGreaterThanEqualAndAtivoTrue(BigDecimal avaliacao);

    /** Ordena restaurantes ativos por avaliação (descendente) */
    List<Restaurante> findByAtivoTrueOrderByAvaliacaoDesc();

    /** Busca restaurantes com taxa de entrega menor ou igual ao valor informado */
    List<Restaurante> findByTaxaEntregaLessThanEqual(BigDecimal taxa);

    /** Retorna os top 5 restaurantes ordenados por nome (alfabética) */
    List<Restaurante> findTop5ByOrderByNomeAsc();

    // =================== CONSULTAS CUSTOMIZADAS ===================

    /** Busca restaurantes ativos que possuem pelo menos um produto cadastrado */
    @Query("SELECT DISTINCT r FROM Restaurante r JOIN r.produtos p WHERE r.ativo = true")
    List<Restaurante> findRestaurantesComProdutos();

    /** Busca restaurantes ativos cuja taxa de entrega esteja dentro de um intervalo */
    @Query("SELECT r FROM Restaurante r WHERE r.taxaEntrega BETWEEN :min AND :max AND r.ativo = true")
    List<Restaurante> findByTaxaEntregaBetween(@Param("min") BigDecimal min, @Param("max") BigDecimal max);

    /** Retorna as categorias distintas de restaurantes ativos, ordenadas */
    @Query("SELECT DISTINCT r.categoria FROM Restaurante r WHERE r.ativo = true ORDER BY r.categoria")
    List<String> findCategoriasDisponiveis();

    // =================== MÉTODOS PAGINADOS ===================

    /** Filtra restaurantes por categoria e status ativo/inativo, com paginação */
    Page<Restaurante> findByCategoriaAndAtivo(String categoria, Boolean ativo, Pageable pageable);

    /** Filtra restaurantes por categoria, com paginação */
    Page<Restaurante> findByCategoria(String categoria, Pageable pageable);

    /** Filtra restaurantes por status ativo/inativo, com paginação */
    Page<Restaurante> findByAtivo(Boolean ativo, Pageable pageable);
}
