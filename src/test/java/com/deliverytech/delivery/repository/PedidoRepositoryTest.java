package com.deliverytech.delivery.repository;



import com.deliverytech.delivery.entity.Pedido;
import com.deliverytech.delivery.entity.Cliente;
import com.deliverytech.delivery.entity.Restaurante;
import com.deliverytech.delivery.enums.StatusPedido;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
public class PedidoRepositoryTest {

    @Autowired
    private PedidoRepository pedidoRepository;

    @BeforeEach
    void setup() {
        // Limpar tabela antes de cada teste
        pedidoRepository.deleteAll();

        // Criar restaurantes de exemplo
        Restaurante r1 = new Restaurante();
        r1.setId(1L);
        r1.setNome("Pizzaria Bella");
        Restaurante r2 = new Restaurante();
        r2.setId(2L);
        r2.setNome("Burger House");

        // Criar cliente de exemplo
        Cliente c1 = new Cliente();
        c1.setId(1L);
        c1.setNome("João Silva");

        // Pedido 1
        Pedido p1 = new Pedido();
        p1.setCliente(c1);
        p1.setRestaurante(r1);
        p1.setValorTotal(new BigDecimal("120"));
        p1.setStatus(StatusPedido.PENDENTE);
        p1.setDataPedido(LocalDateTime.now().minusDays(2));
        pedidoRepository.save(p1);

        // Pedido 2
        Pedido p2 = new Pedido();
        p2.setCliente(c1);
        p2.setRestaurante(r2);
        p2.setValorTotal(new BigDecimal("80"));
        p2.setStatus(StatusPedido.CONFIRMADO);
        p2.setDataPedido(LocalDateTime.now().minusDays(1));
        pedidoRepository.save(p2);

        // Pedido 3
        Pedido p3 = new Pedido();
        p3.setCliente(c1);
        p3.setRestaurante(r1);
        p3.setValorTotal(new BigDecimal("200"));
        p3.setStatus(StatusPedido.PENDENTE);
        p3.setDataPedido(LocalDateTime.now());
        pedidoRepository.save(p3);
    }

    @Test
    void testCalcularTotalVendasPorRestaurante() {
        List<Object[]> resultados = pedidoRepository.calcularTotalVendasPorRestaurante();
        resultados.forEach(linha -> 
            System.out.println("Restaurante: " + linha[0] + ", Total: " + linha[1])
        );
    }

    @Test
    void testBuscarPedidosComValorAcimaDe() {
        BigDecimal valor = new BigDecimal("100");
        List<Pedido> pedidos = pedidoRepository.buscarPedidosComValorAcimaDe(valor);
        pedidos.forEach(p -> 
            System.out.println("Pedido ID: " + p.getId() + ", Valor: " + p.getValorTotal())
        );
    }

    @Test
    void testRelatorioPedidosPorPeriodoEStatus() {
        LocalDateTime inicio = LocalDateTime.now().minusDays(10);
        LocalDateTime fim = LocalDateTime.now().plusDays(1);
        StatusPedido status = StatusPedido.PENDENTE;

        List<Pedido> pedidos = pedidoRepository.relatorioPedidosPorPeriodoEStatus(inicio, fim, status);
        pedidos.forEach(p -> 
            System.out.println("Pedido ID: " + p.getId() + ", Data: " + p.getDataPedido() + ", Status: " + p.getStatus())
        );
    }
}
