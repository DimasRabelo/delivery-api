// package com.deliverytech.delivery.repository;

// import com.deliverytech.delivery.entity.Cliente;
// import com.deliverytech.delivery.entity.ItemPedido;
// import com.deliverytech.delivery.entity.Pedido;
// import com.deliverytech.delivery.entity.Produto;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
// import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

// import java.math.BigDecimal;
// import java.time.LocalDateTime;
// import java.util.List;

// import static org.assertj.core.api.Assertions.assertThat;

// @DataJpaTest
// public class ClienteRepositoryTest {

//     @Autowired
//     private ClienteRepository clienteRepository;

//     @Autowired
//     private TestEntityManager entityManager;

//     @Test
//     @DisplayName("Testar query nativa: ranking de clientes por produtos comprados")
//     void testRankingClientes() {
//         // Criar clientes
//         Cliente ana = new Cliente();
//         ana.setNome("Ana");
//         ana.setAtivo(true);
//         entityManager.persist(ana);

//         Cliente bruno = new Cliente();
//         bruno.setNome("Bruno");
//         bruno.setAtivo(true);
//         entityManager.persist(bruno);

//         Cliente carla = new Cliente();
//         carla.setNome("Carla");
//         carla.setAtivo(true);
//         entityManager.persist(carla);

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

//         // Criar pedidos e itens de pedido
//         Pedido pedido1 = new Pedido();
//         pedido1.setCliente(ana);
//         pedido1.setDataPedido(LocalDateTime.now());
//         entityManager.persist(pedido1);

//         Pedido pedido2 = new Pedido();
//         pedido2.setCliente(bruno);
//         pedido2.setDataPedido(LocalDateTime.now());
//         entityManager.persist(pedido2);

//         Pedido pedido3 = new Pedido();
//         pedido3.setCliente(ana);
//         pedido3.setDataPedido(LocalDateTime.now());
//         entityManager.persist(pedido3);

//         ItemPedido item1 = new ItemPedido();
//         item1.setPedido(pedido1);
//         item1.setProduto(ovo);
//         item1.setQuantidade(5);
//         entityManager.persist(item1);

//         ItemPedido item2 = new ItemPedido();
//         item2.setPedido(pedido1);
//         item2.setProduto(leite);
//         item2.setQuantidade(3);
//         entityManager.persist(item2);

//         ItemPedido item3 = new ItemPedido();
//         item3.setPedido(pedido2);
//         item3.setProduto(pao);
//         item3.setQuantidade(7);
//         entityManager.persist(item3);

//         ItemPedido item4 = new ItemPedido();
//         item4.setPedido(pedido3);
//         item4.setProduto(ovo);
//         item4.setQuantidade(2);
//         entityManager.persist(item4);

//         entityManager.flush();

//         // Executar query
//         List<Object[]> ranking = clienteRepository.rankingClientesPorPedidos();

//         // Mostrar resultado no console
//         System.out.println("=== Ranking Clientes por Produtos Comprados ===");
//         ranking.forEach(c -> System.out.println("Cliente: " + c[0] + " - Total produtos: " + c[1]));

//         // Validações do teste
//         assertThat(ranking).isNotEmpty();
//         assertThat(ranking.size()).isLessThanOrEqualTo(10);

//         // Primeiro do ranking deve ser Ana (5+3+2 = 10 produtos)
//         assertThat((String) ranking.get(0)[0]).isEqualTo("Ana");
//         assertThat(((Number) ranking.get(0)[1]).intValue()).isEqualTo(10);

//         // Segundo Bruno (7 produtos)
//         assertThat((String) ranking.get(1)[0]).isEqualTo("Bruno");
//         assertThat(((Number) ranking.get(1)[1]).intValue()).isEqualTo(7);
//     }
// }
