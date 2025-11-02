package com.deliverytech.delivery.service; 

//  IMPORTS CORRETOS (io.micrometer.tracing) 
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Serviço de exemplo para demonstrar a criação de "Spans" customizados
 * com o Micrometer Tracing (substituto do Sleuth).
 *
 *ATENÇÃO: brave.Tracer está obsoleto.
 * Usamos 'io.micrometer.tracing.Tracer'. A lógica é a mesma.
 */
@Service
public class TracingService {

    private static final Logger log = LoggerFactory.getLogger(TracingService.class);
    
    // 1. O Tracer injetado agora é do Micrometer
    private final Tracer tracer;

    public TracingService(Tracer tracer) {
        this.tracer = tracer;
    }

    /**
     * Simula um processo de negócio complexo com múltiplos "Spans" (etapas).
     * Esta é a tradução 1:1 da lógica do gabarito.
     */
    public void processarPedidoComTracing(String pedidoId) {
        
        // 1. Cria o "Span" PAI (a operação inteira)
        Span spanPai = this.tracer.nextSpan().name("processar-pedido-custom");
        
        // Coloca o Span PAI no "escopo" (contexto) atual
        try (Tracer.SpanInScope ws = this.tracer.withSpan(spanPai.start())) {
            
            spanPai.tag("pedido.id", pedidoId);
            log.info("Iniciando processamento do pedido (Span PAI)");

            // 2. Chama os métodos que criam "Spans" FILHOS
            validarPedidoComSpan(pedidoId);
            calcularFreteComSpan(pedidoId);
            processarPagamentoComSpan(pedidoId);
            
            log.info("Finalizando processamento do pedido (Span PAI)");

        } catch (Exception e) {
            spanPai.error(e); // Marca o Span PAI com erro
        } finally {
            spanPai.end(); // Fecha o Span PAI
        }
    }

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