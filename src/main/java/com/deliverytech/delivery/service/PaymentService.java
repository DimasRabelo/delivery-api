package com.deliverytech.delivery.service;

public interface PaymentService {
    
    /**
     * Simula o processamento de um pagamento online externo.
     *
     * @param metodoPagamento O método escolhido pelo cliente (PIX, CARTAO, DINHEIRO).
     * @param valorTotal O valor do pedido.
     * @return true se a transação for bem-sucedida (simulada), false caso contrário.
     */
    boolean processPayment(String metodoPagamento, double valorTotal);
}
