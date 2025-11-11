package com.deliverytech.delivery.service.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Serviço centralizado para gerenciamento e exposição de métricas da aplicação
 * usando o Micrometer.
 */
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
    // Usamos Atomic para garantir segurança em ambientes concorrentes
    private final AtomicInteger usuariosAtivos = new AtomicInteger(0);
    private final AtomicLong produtosEmEstoque = new AtomicLong(1000);

    
    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // --- Inicialização dos Contadores ---
        this.pedidosProcessados = Counter.builder("delivery.pedidos.total")
                .description("Total de pedidos processados")
                .register(meterRegistry);
        
        this.pedidosComSucesso = Counter.builder("delivery.pedidos.sucesso")
                .description("Pedidos processados com sucesso")
                .register(meterRegistry);
        
        this.pedidosComErro = Counter.builder("delivery.pedidos.erro")
                .description("Pedidos com erro no processamento")
                .register(meterRegistry);
        
        this.receitaTotal = Counter.builder("delivery.receita.total")
                .description("Receita total em centavos")
                .baseUnit("centavos") // Define a unidade base
                .register(meterRegistry);


        // --- Inicialização dos Timers ---
        this.tempoProcessamentoPedido = Timer.builder("delivery.pedido.processamento.tempo")
                .description("Tempo de processamento de pedidos")
                .register(meterRegistry);
        
        this.tempoConsultaBanco = Timer.builder("delivery.database.consulta.tempo")
                .description("Tempo de consulta ao banco de dados")
                .register(meterRegistry);

        // --- Inicialização dos Gauges ---
        // O Gauge monitora um valor que pode subir ou descer
        Gauge.builder("delivery.usuarios.ativos", usuariosAtivos, AtomicInteger::get)
                .description("Número de usuários ativos")
                .register(meterRegistry);
        
        Gauge.builder("delivery.produtos.estoque", produtosEmEstoque, AtomicLong::get)
                .description("Produtos em estoque")
                .register(meterRegistry);
    }

    // --- Métodos para Contadores ---
    
    /**
     * Incrementa o contador total de pedidos processados.
     */
    public void incrementarPedidosProcessados() {
        pedidosProcessados.increment();
    }

    /**
     * Incrementa o contador de pedidos bem-sucedidos.
     */
    public void incrementarPedidosComSucesso() {
        pedidosComSucesso.increment();
    }

    /**
     * Incrementa o contador de pedidos com falha.
     */
    public void incrementarPedidosComErro() {
        pedidosComErro.increment();
    }

    /**
     * Adiciona um valor (em Reais) ao contador de receita.
     * O valor será convertido para centavos para registro.
     * @param valor Valor do pedido em Reais (ex: 25.50)
     */
    public void adicionarReceita(double valor) {
        // Converte para centavos para evitar problemas com ponto flutuante
        receitaTotal.increment(valor * 100); 
    }

    // --- Métodos para Timers ---
    
    /**
     * Inicia um 'sample' de timer para o processamento de pedido.
     * @return Um Sample que deve ser parado com 'finalizarTimerPedido'.
     */
    public Timer.Sample iniciarTimerPedido() {
        return Timer.start(meterRegistry);
    }

    /**
     * Finaliza o 'sample' de timer para o processamento de pedido.
     * @param sample O Sample retornado por 'iniciarTimerPedido'.
     */
    public void finalizarTimerPedido(Timer.Sample sample) {
        sample.stop(tempoProcessamentoPedido);
    }

    /**
     * Inicia o timer para medir o tempo de consulta ao banco.
     * @return Um Sample que deve ser parado com 'finalizarTimerBanco'.
     */
    public Timer.Sample iniciarTimerBanco() {
        return Timer.start(meterRegistry);
    }
    
    /**
     * Finaliza o timer de consulta ao banco.
     * @param sample O Sample retornado por 'iniciarTimerBanco'.
     */
    public void finalizarTimerBanco(Timer.Sample sample) {
        sample.stop(tempoConsultaBanco);
    }

    // --- Métodos para Gauges ---

    /**
     * Atualiza o medidor (Gauge) de usuários ativos.
     * @param quantidade O número atual de usuários ativos.
     */
    public void setUsuariosAtivos(int quantidade) {
        usuariosAtivos.set(quantidade);
    }

    /**
     * Atualiza o medidor (Gauge) de produtos em estoque.
     * @param quantidade O número atual de produtos em estoque.
     */
    public void setProdutosEmEstoque(long quantidade) {
        produtosEmEstoque.set(quantidade);
    }
}