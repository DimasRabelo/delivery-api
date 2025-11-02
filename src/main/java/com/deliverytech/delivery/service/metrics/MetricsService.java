package com.deliverytech.delivery.service.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class MetricsService {

    private final MeterRegistry meterRegistry;

    // --- Contadores (Counters) ---
    private final Counter pedidosProcessados;
    private final Counter pedidosComSucesso;
    private final Counter pedidosComErro;
    private final Counter receitaTotal;

    // --- Cronômetros (Timers) ---
    private final Timer tempoProcessamentoPedido;
    private final Timer tempoConsultaBanco; 

    // --- Medidores (Gauges) ---
    private final AtomicInteger usuariosAtivos = new AtomicInteger(0);
    private final AtomicLong produtosEmEstoque = new AtomicLong(1000);

    
    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // --- Inicialização dos Contadores ---
        this.pedidosProcessados = Counter.builder("delivery.pedidos.total")
                .description("Total de pedidos processados")
                .register(meterRegistry);
        // ... (outros contadores) ...
        this.pedidosComSucesso = Counter.builder("delivery.pedidos.sucesso")
                .description("Pedidos processados com sucesso")
                .register(meterRegistry);
        this.pedidosComErro = Counter.builder("delivery.pedidos.erro")
                .description("Pedidos com erro no processamento")
                .register(meterRegistry);
        this.receitaTotal = Counter.builder("delivery.receita.total")
                .description("Receita total em centavos")
                .baseUnit("centavos")
                .register(meterRegistry);


        // --- Inicialização dos Timers ---
        this.tempoProcessamentoPedido = Timer.builder("delivery.pedido.processamento.tempo")
                .description("Tempo de processamento de pedidos")
                .register(meterRegistry);
        
        this.tempoConsultaBanco = Timer.builder("delivery.database.consulta.tempo")
                .description("Tempo de consulta ao banco de dados")
                .register(meterRegistry);

        // --- Inicialização dos Gauges ---
        Gauge.builder("delivery.usuarios.ativos", usuariosAtivos, AtomicInteger::get)
                .description("Número de usuários ativos")
                .register(meterRegistry);
        Gauge.builder("delivery.produtos.estoque", produtosEmEstoque, AtomicLong::get)
                .description("Produtos em estoque")
                .register(meterRegistry);
    }

    // --- Métodos para Contadores ---
    
    public void incrementarPedidosProcessados() {
        pedidosProcessados.increment();
    }
    public void incrementarPedidosComSucesso() {
        pedidosComSucesso.increment();
    }
    public void incrementarPedidosComErro() {
        pedidosComErro.increment();
    }
    public void adicionarReceita(double valor) {
        receitaTotal.increment(valor * 100); 
    }

    // --- Métodos para Timers ---
    
    public Timer.Sample iniciarTimerPedido() {
        return Timer.start(meterRegistry);
    }
    public void finalizarTimerPedido(Timer.Sample sample) {
        sample.stop(tempoProcessamentoPedido);
    }

    // ==========================================================
    // ⬇️ CORREÇÃO DA "TAREFA BÔNUS" AQUI ⬇️
    // ==========================================================
    /**
     * Inicia o timer para medir o tempo de consulta ao banco.
     */
    public Timer.Sample iniciarTimerBanco() {
        return Timer.start(meterRegistry);
    }
    
    /**
     * Finaliza o timer de consulta ao banco.
     * O aviso "is not used" no campo tempoConsultaBanco vai sumir!
     */
    public void finalizarTimerBanco(Timer.Sample sample) {
        sample.stop(tempoConsultaBanco);
    }
    // ==========================================================


    // --- Métodos para Gauges ---
    public void setUsuariosAtivos(int quantidade) {
        usuariosAtivos.set(quantidade);
    }
    public void setProdutosEmEstoque(long quantidade) {
        produtosEmEstoque.set(quantidade);
    }
}