package com.deliverytech.delivery.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema; // IMPORT ADICIONADO
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
// import jakarta.validation.constraints.Size; // (Não é mais necessário para 'nome')
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO para a atualização de dados de AUTENTICAÇÃO de um usuário.
 * (Refatorado para a "Decisão 1")
 *
 * @implNote Não contém 'nome', pois 'nome' é um dado de Perfil (Cliente)
 * e deve ser atualizado pelo ClienteService/ClienteDTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para atualizar dados de autenticação (email) do Usuário")
public class UsuarioUpdateDTO {

    // --- CAMPO 'nome' REMOVIDO ---
    // @NotBlank(message = "O nome é obrigatório")
    // @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
    // private String nome;

    @Schema(description = "O novo email do usuário", required = true) // Schema adicionado
    @NotBlank(message = "O email é obrigatório")
    @Email(message = "O formato do email é inválido")
    private String email;

}