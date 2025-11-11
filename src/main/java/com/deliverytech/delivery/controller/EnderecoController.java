package com.deliverytech.delivery.controller;

import com.deliverytech.delivery.dto.request.EnderecoDTO;
import com.deliverytech.delivery.dto.response.EnderecoResponseDTO;
import com.deliverytech.delivery.entity.Endereco;
import com.deliverytech.delivery.entity.Usuario;
import com.deliverytech.delivery.service.EnderecoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter; // --- 1. IMPORTADO ---
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication; // --- 2. IMPORTADO ---
import org.springframework.validation.annotation.Validated; // --- 3. IMPORTADO ---
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

// --- 4. ADICIONADO ---
@Validated 
@RestController
@RequestMapping("/api/enderecos") 
@RequiredArgsConstructor
public class EnderecoController {

    private final EnderecoService enderecoService;
    private final ModelMapper modelMapper;

    /**
     * Endpoint para listar os endereços do usuário logado (ex: Cliente)
     */
    @GetMapping("/meus")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<List<EnderecoResponseDTO>> listarMeusEnderecos() {
        
        List<Endereco> enderecos = enderecoService.buscarPorUsuarioLogado();
        
        List<EnderecoResponseDTO> dtos = enderecos.stream()
                .map(endereco -> modelMapper.map(endereco, EnderecoResponseDTO.class))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }

    /**
     * Endpoint para criar um novo endereço para o usuário logado
     */
    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<EnderecoDTO> adicionarNovoEndereco(@Valid @RequestBody EnderecoDTO enderecoDTO) {
        
        Endereco novoEndereco = enderecoService.salvarNovoEndereco(enderecoDTO);
        
        EnderecoDTO dtoSalvo = modelMapper.map(novoEndereco, EnderecoDTO.class);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(dtoSalvo);
    }

    
    /**
     * Endpoint para DELETAR um endereço existente do usuário logado
     * (Seu código estava correto, só faltavam os imports)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CLIENTE')") 
    @Operation(summary = "Deleta um endereço (Próprio)",
               description = "Deleta um endereço específico pertencente ao usuário logado. Requer role CLIENTE.")
    public ResponseEntity<Void> deletarEndereco(
            @Parameter(description = "ID do endereço a ser deletado") 
            @PathVariable @Positive Long id,
            Authentication authentication) {
        
        // 1. Pega o usuário que está logado
        Usuario usuarioLogado = (Usuario) authentication.getPrincipal();

        // 2. Chama o serviço, que fará a verificação de propriedade e a exclusão
        // (Lembre-se de implementar 'deletarEndereco' no seu EnderecoService!)
        enderecoService.deletarEndereco(id, usuarioLogado.getId());

        // 3. Retorna 204 No Content (Sucesso, sem corpo)
        return ResponseEntity.noContent().build();
    }
}