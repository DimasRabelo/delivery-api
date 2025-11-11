
package com.deliverytech.delivery.exception;

/**
 * Exceção específica para conflitos de dados, como duplicidade de registros.
 * Extende BusinessException para manter o padrão de tratamento de erros da aplicação.
 */
public class ConflictException extends BusinessException {

    // Nome do campo que gerou o conflito (ex: "nome")
    private String conflictField;

    // Valor que gerou o conflito (ex: "Restaurante Teste")
    private Object conflictValue;

    /**
     * Construtor simples com apenas a mensagem de erro.
     * Define automaticamente o errorCode como "CONFLICT".
     * 
     * @param message Mensagem de erro descritiva
     */
    public ConflictException(String message) {
        super(message);
        this.setErrorCode("CONFLICT");
    }

    /**
     * Construtor completo para informar campo e valor do conflito.
     * Útil para retornar detalhes estruturados na resposta da API.
     * 
     * @param message Mensagem de erro descritiva
     * @param conflictField Nome do campo que gerou o conflito
     * @param conflictValue Valor que gerou o conflito
     */
    public ConflictException(String message, String conflictField, Object conflictValue) {
        super(message);
        this.conflictField = conflictField;
        this.conflictValue = conflictValue;
        this.setErrorCode("CONFLICT");
    }

    /**
     * Retorna o nome do campo que causou o conflito
     * 
     * @return Nome do campo
     */
    public String getConflictField() {
        return conflictField;
    }

    /**
     * Retorna o valor que gerou o conflito
     * 
     * @return Valor conflitante
     */
    public Object getConflictValue() {
        return conflictValue;
    }
}
