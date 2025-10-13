// package com.deliverytech.delivery.repository;

// import com.deliverytech.delivery.entity.ItemPedido;
// import com.deliverytech.delivery.entity.Pedido;
// import com.deliverytech.delivery.entity.Produto;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
// import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

// import java.math.BigDecimal;
// import java.util.List;

// import static org.assertj.core.api.Assertions.assertThat;

// @DataJpaTest
// public class ProdutoRepositoryTest {

//     @Autowired
//     private ProdutoRepository produtoRepository;

//     @Autowired
//     private TestEntityManager entityManager;

//     @Test
//     @DisplayName("Testar query nativa: top 5 produtos mais vendidos")
//     void testProdutosMaisVendidos() {
//         // Criar produtos
//         Produto ovo = new Produto();
//         ovo.setNome("Ovo");
//         ovo.setPreco(new BigDecimal("5.00"));
//         entityManager.persist(ovo);

//         Produto leite = new Produto();
//         leite.setNome("Leite");
//         leite.setPreco(new BigDecimal("12.50"));
//         entityManager.persist(leite);

//         Produto pao = new Produto();
//         pao.setNome("Pão");
//         pao.setPreco(new BigDecimal("3.00"));
//         entityManager.persist(pao);

//         // Criar pedidos
//         Pedido pedido1 = new Pedido();
//         entityManager.persist(pedido1);

//         Pedido pedido2 = new Pedido();
//         entityManager.persist(pedido2);

//         // Criar itens de pedido simulando vendas
//         ItemPedido item1 = new ItemPedido();
//         item1.setProduto(ovo);
//         item1.setPedido(pedido1);
//         item1.setQuantidade(10);
//         entityManager.persist(item1);

//         ItemPedido item2 = new ItemPedido();
//         item2.setProduto(leite);
//         item2.setPedido(pedido1);
//         item2.setQuantidade(5);
//         entityManager.persist(item2);

//         ItemPedido item3 = new ItemPedido();
//         item3.setProduto(pao);
//         item3.setPedido(pedido2);
//         item3.setQuantidade(20);
//         entityManager.persist(item3);

//         entityManager.flush();

//         // Executar query
//         List<Object[]> ranking = produtoRepository.produtosMaisVendidos();

//         // Mostrar resultado no console
// System.out.println("=== Ranking Produtos Mais Vendidos ===");
// ranking.forEach(p -> System.out.println("Produto: " + p[0] + " - Quantidade vendida: " + p[1]));

//         assertThat(ranking).isNotEmpty();
//         assertThat(ranking.size()).isLessThanOrEqualTo(5);

//         // Primeiro do ranking deve ser "Pão" (20 vendidos)
//         assertThat((String) ranking.get(0)[0]).isEqualTo("Pão");
//         assertThat(((Number) ranking.get(0)[1]).intValue()).isEqualTo(20);

//         // Segundo "Ovo" (10 vendidos)
//         assertThat((String) ranking.get(1)[0]).isEqualTo("Ovo");
//         assertThat(((Number) ranking.get(1)[1]).intValue()).isEqualTo(10);

//         // Terceiro "Leite" (5 vendidos)
//         assertThat((String) ranking.get(2)[0]).isEqualTo("Leite");
//         assertThat(((Number) ranking.get(2)[1]).intValue()).isEqualTo(5);
//     }
// }
