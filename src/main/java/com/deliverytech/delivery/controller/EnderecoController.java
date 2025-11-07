package com.deliverytech.delivery.controller;

import com.deliverytech.delivery.dto.request.EnderecoDTO;
import com.deliverytech.delivery.entity.Endereco;
import com.deliverytech.delivery.service.EnderecoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/enderecos") // Padronizando o prefixo da API
@RequiredArgsConstructor
public class EnderecoController {

    private final EnderecoService enderecoService;
    private final ModelMapper modelMapper;

    /**
     * Endpoint para listar os endereços do usuário logado (ex: Cliente)
     */
    @GetMapping("/meus")
    @PreAuthorize("hasRole('CLIENTE')") // Apenas CLIENTES podem chamar este método
    public ResponseEntity<List<EnderecoDTO>> listarMeusEnderecos() {
        
        List<Endereco> enderecos = enderecoService.buscarPorUsuarioLogado();
        
        // Converte a lista de Entidades (do banco) para DTOs (para o Postman/Frontend)
        List<EnderecoDTO> dtos = enderecos.stream()
                .map(endereco -> modelMapper.map(endereco, EnderecoDTO.class))
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
        
        // Converte a entidade salva em DTO para retornar na resposta
        EnderecoDTO dtoSalvo = modelMapper.map(novoEndereco, EnderecoDTO.class);
        
        // Retorna 201 Created
        return ResponseEntity.status(HttpStatus.CREATED).body(dtoSalvo);
    }
}