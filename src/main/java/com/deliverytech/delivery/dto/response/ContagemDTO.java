package com.deliverytech.delivery.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO (Data Transfer Object) genérico usado para
 * retornar um valor de contagem (long).
 */
public class ContagemDTO {

    @Schema(description = "A contagem total de itens", example = "5")
    private long contagem;

    public ContagemDTO(long contagem) {
        this.contagem = contagem;
    }

    public long getContagem() {
        return contagem;
    }

    public void setContagem(long contagem) {
        this.contagem = contagem;
    }
}