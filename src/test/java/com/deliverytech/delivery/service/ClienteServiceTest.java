// package com.deliverytech.delivery.service;

// import com.deliverytech.delivery.dto.ClienteDTO;
// import com.deliverytech.delivery.dto.ClienteResponseDTO;
// import com.deliverytech.delivery.entity.Cliente;
// import com.deliverytech.delivery.exception.BusinessException;
// import com.deliverytech.delivery.repository.ClienteRepository;
// import com.deliverytech.delivery.service.impl.ClienteServiceImpl;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.modelmapper.ModelMapper;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.*;
// import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class)
// class ClienteServiceTest {

//     @Mock
//     private ClienteRepository clienteRepository;

//     @Mock
//     private ModelMapper modelMapper;

//     @InjectMocks
//     private ClienteServiceImpl clienteService;

//     @Test
//     void deveCadastrarClienteComSucesso() {
//         // Given
//         ClienteDTO dto = new ClienteDTO();
//         dto.setNome("João Silva");
//         dto.setEmail("joao@email.com");
//         dto.setTelefone("11999999999");
//         dto.setEndereco("Rua A, 123");

//         Cliente cliente = new Cliente();
//         cliente.setId(1L);
//         cliente.setNome("João Silva");
//         cliente.setEmail("joao@email.com");
//         cliente.setAtivo(true);

//         ClienteResponseDTO responseDTO = new ClienteResponseDTO();
//         responseDTO.setId(1L);
//         responseDTO.setNome("João Silva");
//         responseDTO.setEmail("joao@email.com");
//         responseDTO.setAtivo(true);

//         when(clienteRepository.existsByEmail(dto.getEmail())).thenReturn(false);
//         when(modelMapper.map(dto, Cliente.class)).thenReturn(cliente);
//         when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
//         when(modelMapper.map(cliente, ClienteResponseDTO.class)).thenReturn(responseDTO);

//         // When
//         ClienteResponseDTO resultado = clienteService.cadastrarCliente(dto);

//         // Then
//         assertNotNull(resultado);
//         assertEquals("João Silva", resultado.getNome());
//         assertEquals("joao@email.com", resultado.getEmail());
//         assertTrue(resultado.isAtivo());

//         verify(clienteRepository).existsByEmail(dto.getEmail());
//         verify(clienteRepository).save(any(Cliente.class));
//     }

//     @Test
//     void deveRejeitarClienteComEmailDuplicado() {
//         // Given
//         ClienteDTO dto = new ClienteDTO();
//         dto.setEmail("joao@email.com");

//         when(clienteRepository.existsByEmail(dto.getEmail())).thenReturn(true);

//         // When & Then
//         BusinessException exception = assertThrows(
//                 BusinessException.class,
//                 () -> clienteService.cadastrarCliente(dto)
//         );

//         assertEquals("Email já cadastrado: joao@email.com", exception.getMessage());
//         verify(clienteRepository, never()).save(any(Cliente.class));
//     }
// }
