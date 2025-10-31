package com.deliverytech.delivery.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO para a atualização de dados de um usuário.
 * Contém apenas os campos que um usuário (ou admin) pode modificar
 * através do endpoint principal de atualização.
 *
 * @implNote Não contém 'senha', 'role' ou 'ativo' para
 * prevenir vulnerabilidades de "Mass Assignment".
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioUpdateDTO {

    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
    private String nome;

    @NotBlank(message = "O email é obrigatório")
    @Email(message = "O formato do email é inválido")
    private String email;

}