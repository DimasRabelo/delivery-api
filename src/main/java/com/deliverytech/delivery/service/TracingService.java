package com.deliverytech.delivery.service;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Serviço responsável por criar spans customizados com Micrometer Tracing.
 * Substitui o uso obsoleto de 'brave.Tracer' (Sleuth).
 * 
 * Cada método demonstra como criar spans pai e filhos para monitoramento detalhado.
 */
@Service
public class TracingService {

    private static final Logger log = LoggerFactory.getLogger(TracingService.class);

    private final Tracer tracer;

    public TracingService(Tracer tracer) {
        this.tracer = tracer;
    }

    // ==========================================================
    // --- MÉTODO PÚBLICO PRINCIPAL (PROCESSAMENTO DE PEDIDO) ---
    // ==========================================================
    /**
     * Processa um pedido simulando várias etapas, cada uma com seu Span.
     * @param pedidoId ID do pedido
     */
    public void processarPedidoComTracing(String pedidoId) {

        // --- Span PAI: operação completa ---
        Span spanPai = this.tracer.nextSpan().name("processar-pedido-custom");

        try (Tracer.SpanInScope ws = this.tracer.withSpan(spanPai.start())) {

            spanPai.tag("pedido.id", pedidoId);
            log.info("Iniciando processamento do pedido (Span PAI)");

            // --- Chamadas de métodos que criam Spans FILHOS ---
            validarPedidoComSpan(pedidoId);
            calcularFreteComSpan(pedidoId);
            processarPagamentoComSpan(pedidoId);

            log.info("Finalizando processamento do pedido (Span PAI)");

        } catch (Exception e) {
            spanPai.error(e); // Marca o Span PAI como erro
        } finally {
            spanPai.end();
        }
    }

    // ==========================================================
    // --- MÉTODOS PRIVADOS (ETAPAS DO PEDIDO) ---
    // ==========================================================

    private void validarPedidoComSpan(String pedidoId) {
        Span spanFilho = this.tracer.nextSpan().name("validar-pedido");
        try (Tracer.SpanInScope ws = this.tracer.withSpan(spanFilho.start())) {
            spanFilho.tag("pedido.id", pedidoId);
            log.info("Validando pedido (Span FILHO)");
            Thread.sleep(50); // Simula trabalho
            spanFilho.tag("validacao.resultado", "sucesso");
        } catch (Exception e) {
            spanFilho.error(e);
        } finally {
            spanFilho.end();
        }
    }

    private void calcularFreteComSpan(String pedidoId) {
        Span spanFilho = this.tracer.nextSpan().name("calcular-frete");
        try (Tracer.SpanInScope ws = this.tracer.withSpan(spanFilho.start())) {
            spanFilho.tag("pedido.id", pedidoId);
            log.info("Calculando frete (Span FILHO)");
            Thread.sleep(30); // Simula trabalho
            spanFilho.tag("frete.valor", "15.50");
        } catch (Exception e) {
            spanFilho.error(e);
        } finally {
            spanFilho.end();
        }
    }

    private void processarPagamentoComSpan(String pedidoId) {
        Span spanFilho = this.tracer.nextSpan().name("processar-pagamento");
        try (Tracer.SpanInScope ws = this.tracer.withSpan(spanFilho.start())) {
            spanFilho.tag("pedido.id", pedidoId);
            log.info("Processando pagamento (Span FILHO)");
            Thread.sleep(100); // Simula trabalho
            spanFilho.tag("pagamento.status", "aprovado");
        } catch (Exception e) {
            spanFilho.error(e);
        } finally {
            spanFilho.end();
        }
    }
}
