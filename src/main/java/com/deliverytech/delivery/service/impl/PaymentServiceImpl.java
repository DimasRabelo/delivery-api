package com.deliverytech.delivery.service.impl;

import com.deliverytech.delivery.service.PaymentService;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Override
    public boolean processPayment(String metodoPagamento, double valorTotal) {
        // --- ESTE É O SEU MOCK SERVICE DEDICADO ---
        
        // Simulação: Se o pagamento for ONLINE (PIX/Cartão), ele SIMULA SUCESSO.
        if ("PIX".equalsIgnoreCase(metodoPagamento) || "CARTAO DE CREDITO".equalsIgnoreCase(metodoPagamento) || "CARTAO DE DEBITO".equalsIgnoreCase(metodoPagamento)) {
            
            // Em um cenário real, o pagamento seria validado com um gateway.
            System.out.println(">>> MOCK SERVICE: Pagamento online (" + metodoPagamento + ") simulado com SUCESSO.");
            return true; 
        }
        
        // Simulação: Pagamento em Dinheiro não precisa de validação de gateway, apenas avança.
        if ("DINHEIRO".equalsIgnoreCase(metodoPagamento)) {
             System.out.println(">>> MOCK SERVICE: Pagamento em dinheiro registrado. Não requer validação online.");
            return true; 
        }

        // Caso o método não seja reconhecido ou o valor seja inválido (simulando falha)
        System.out.println(">>> MOCK SERVICE: Pagamento FAILED. Motivo: Simulação de Erro de Transação.");
        return false;
    }
}