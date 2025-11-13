package com.deliverytech.delivery.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Dados para atualização de usuário")
public class UsuarioUpdateDTO {

    @Schema(description = "Novo email", example = "novo@email.com")
    private String email;

    @Schema(description = "Nova senha", example = "123456")
    private String senha; // <--- Adicionado para corrigir o erro getSenha()

    @Schema(description = "Novo nome", example = "João Silva")
    private String nome;  // <--- Adicionado para corrigir o erro getNome()
}