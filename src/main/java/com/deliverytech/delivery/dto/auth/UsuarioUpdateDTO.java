package com.deliverytech.delivery.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * DTO (Data Transfer Object) usado para receber dados no corpo de requisições
 * de atualização de informações do usuário.
 */
@Data // Anotação do Lombok que gera automaticamente getters, setters, toString, equals e hashCode.
@Schema(description = "Dados para atualização de usuário") // Documentação Swagger/OpenAPI para este objeto
public class UsuarioUpdateDTO {

    /**
     * Novo email.
     * O campo é opcional, dependendo da lógica de negócio.
     */
    @Schema(description = "Novo email", example = "novo@email.com")
    private String email;

    /**
     * Nova senha.
     * Necessária para o endpoint, especialmente se houver validação de segurança ao atualizar outros campos.
     * Se for usada para alterar a senha, deve ser encriptada no Service.
     */
    @Schema(description = "Nova senha", example = "123456")
    private String senha; // Campo para a nova senha

    /**
     * Novo nome completo do usuário.
     */
    @Schema(description = "Novo nome", example = "João Silva")
    private String nome;  // Campo para o novo nome
}