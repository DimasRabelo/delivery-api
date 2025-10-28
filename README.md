🍔 DeliveryTech API
Sistema de delivery robusto desenvolvido com Spring Boot 3 e Java 21, focado em alta performance e segurança.

Este projeto implementa uma API REST completa para gerenciar clientes, restaurantes, produtos e pedidos, com uma camada de segurança granular usando Spring Security 6 e autenticação stateless via JSON Web Tokens (JWT).

O sistema controla o acesso baseado em perfis (ADMIN, RESTAURANTE, CLIENTE), garante a propriedade dos dados (ex: um restaurante só pode gerenciar seus próprios produtos) e expõe uma documentação profissional com Swagger/OpenAPI.

🚀 Tecnologias Utilizadas
Java 21 LTS

Spring Boot 3.5.6

Spring Web: Para construção de endpoints REST.

Spring Data JPA: Para persistência de dados.

Spring Validation: Para validação de DTOs.

Spring Security 6: Para Autenticação e Autorização.

JWT (JSON Web Tokens): Para gerenciamento de sessão stateless (via biblioteca jjwt).

H2 Database: Banco de dados relacional em memória para desenvolvimento e testes.

springdoc-openapi (Swagger): Para documentação interativa da API.

Maven: Para gerenciamento de dependências.

🏗️ Arquitetura
A aplicação segue uma arquitetura em camadas, agora com o JwtAuthenticationFilter como o "portão de entrada" para requisições protegidas.

Snippet de código

graph TD
    A[App Mobile / Portal Web] -->|HTTP REST| B(JwtAuthenticationFilter);
    B -->|Token Válido?| C{Controllers};
    C -->|Valida DTOs e @PreAuthorize| D[Services];
    D -->|Define Regras de Negócio e @Transactional| E[Repositories];
    E -->|Executa Queries (JPA)| F[Banco de Dados (H2)];
    
    subgraph "Camada de Segurança (Spring Security)"
        B
        G(SecurityConfig)
        H(JwtUtil)
        I(AuthService/UserDetailsService)
    end
## 🏗️ Estrutura de Pastas

A estrutura do projeto foi organizada para refletir a separação de responsabilidades, com um novo pacote `security` dedicado:

```text
📦src
 ┣ 📂main
 ┃ ┣ 📂java
 ┃ ┃ ┗ 📂com
 ┃ ┃ ┃ ┗ 📂deliverytech
 ┃ ┃ ┃ ┃ ┗ 📂delivery
 ┃ ┃ ┃ ┃ ┃ ┣ 📂config
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ModelMapperConfig.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜SecurityConfig.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜SwaggerConfig.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📂controller
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂auth
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜AuthController.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜UsuarioController.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ClienteController.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜PedidoController.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ProdutoController.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜RelatorioController.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜RestauranteController.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📂dto
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂auth
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜LoginRequest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜LoginResponse.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜RegisterRequest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜UserResponse.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂relatorio
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜RelatorioClientesDTO.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜RelatorioPedidosDTO.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜RelatorioProdutosDTO.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜RelatorioVendasDTO.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂response
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ApiResponseWrapper.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜CalculoPedidoDTO.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜CalculoPedidoResponseDTO.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ClienteResponseDTO.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ErrorResponse.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜PagedResponseWrapper.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜PedidoResponseDTO.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ProdutoResponseDTO.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜RestauranteResponseDTO.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ClienteDTO.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ItemPedidoDTO.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜PedidoDTO.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ProdutoDTO.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜RestauranteDTO.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜StatusPedidoDTO.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📂entity
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜Cliente.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ItemPedido.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜Pedido.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜Produto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜Restaurante.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜Usuario.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📂enums
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜Role.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜StatusPedido.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📂exception
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜BusinessException.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ConflictException.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜EntityNotFoundException.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜GlobalExceptionHandler.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜ValidationException.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📂repository
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂auth
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜UsuarioRepository.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ClienteRepository.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜PedidoRepository.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ProdutoRepository.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜RestauranteRepository.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📂security
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📂jwt
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜JwtAuthenticationFilter.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜JwtUtil.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜SecurityUtils.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📂service
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂auth
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜AuthService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜UsuarioService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂impl
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ClienteServiceImpl.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜PedidoServiceImpl.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ProdutoServiceImpl.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜RelatorioServiceImpl.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜RestauranteServiceImpl.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ClienteService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜PedidoService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ProdutoService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜RelatorioService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜RestauranteService.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📂validation
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜CEPValidator.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜CategoriaValidator.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜TelefoneValidator.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ValidCEP.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ValidCategoria.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ValidHorarioFuncionamento.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜ValidTelefone.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📜DeliveryApiApplication.java
 ┃ ┃ ┃ ┃ ┃ ┗ 📜GerarSenha.java
 ┃ ┣ 📂resources
 ┃ ┃ ┣ 📜_data.sql
 ┃ ┃ ┣ 📜application.properties
 ┃ ┃ ┗ 📜data.sql
 ┃ ┗ 📜estrutura=projeto.png
 ┣ 📂postman
 ┃ ┣ 📜DeliveryApi.postman_collection.json
 ┃ ┣ 📜DeliveryApiLogin.postman_collection.json
 ┃ ┣ 📜DeliveryApiTestValidation.postman_collection.json
 ┃ ┗ 📜Relatorios Delivery API.postman_collection.json
 ┗ 📂test
 ┃ ┗ 📂java
 ┃ ┃ ┗ 📂com
 ┃ ┃ ┃ ┗ 📂deliverytech
 ┃ ┃ ┃ ┃ ┗ 📂delivery
 ┃ ┃ ┃ ┃ ┃ ┣ 📂controller
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ClienteControllerIT.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜PedidoControllerIT.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ProdutoControllerIT.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜RestauranteControllerIT.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📂repository
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ClienteRepositoryTest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜PedidoRepositoryTest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜ProdutoRepositoryTest.java
 ┃ ┃ ┃ ┃ ┃ ┗ 📂service
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜ClienteServiceTest.java
⚙️ Funcionalidades Implementadas
🔐 Segurança (Spring Security + JWT)
Autenticação Stateless: Autenticação via Bearer Token (JWT).

Autorização Granular: Uso de @PreAuthorize para controle de acesso em nível de método, diferenciando ADMIN, RESTAURANTE e CLIENTE.

Verificação de Propriedade: Lógica de serviço (ex: @produtoService.isOwner(#id)) que garante que um usuário RESTAURANTE só possa editar seus próprios recursos.

Endpoints de Autenticação: POST /api/auth/login, POST /api/auth/register e GET /api/auth/me.

Hashing de Senhas: Senhas são armazenadas usando BCryptPasswordEncoder.

Tratamento de Exceções: Respostas 401 (Unauthorized) e 403 (Forbidden) customizadas e padronizadas.

🛠️ Services (Regras de Negócio)
AuthService: Implementa UserDetailsService para carregar usuários e gerencia o registro.

RestauranteService: Cadastro, filtros, cálculo de taxa de entrega e verificação de propriedade (isOwner).

ProdutoService: Gerenciamento de cardápio e verificação de propriedade (isOwner).

PedidoService: Lógica complexa para criação de pedidos, cálculo de total, atualização de status e verificação de acesso (canAccess).

RelatorioService: Geração de relatórios de vendas, produtos, clientes, etc.

📦 DTOs e Validações
Auth DTOs: LoginRequest, LoginResponse (com token), RegisterRequest, UserResponse (DTO seguro, sem senha).

Request DTOs: ClienteDTO, RestauranteDTO, ProdutoDTO, PedidoDTO, ItemPedidoDTO.

Response DTOs: ClienteResponseDTO, RestauranteResponseDTO, ProdutoResponseDTO, PedidoResponseDTO, etc.

Validações: @Valid, @NotNull, @NotBlank, @Email, @Size, e validações customizadas.

📋 Endpoints REST (Principais)
A API é dividida em endpoints públicos (para consulta) e protegidos (que exigem autenticação e autorização).

Base URL: http://localhost:8080/api

🔑 Autenticação (Público)
POST /auth/login: Autentica um usuário e retorna um token JWT.

POST /auth/register: Registra um novo usuário (CLIENTE ou RESTAURANTE).

🍽️ Endpoints Públicos (Consulta)
GET /restaurantes: Lista restaurantes (com filtros).

GET /restaurantes/{id}: Busca um restaurante por ID.

GET /restaurantes/{id}/produtos: Lista o cardápio (produtos) de um restaurante.

GET /restaurantes/{id}/taxa-entrega/{cep}: Calcula a taxa de entrega.

GET /produtos/{id}: Busca um produto por ID.

POST /pedidos/calcular: Calcula o total de um pedido (sem salvar).

🛡️ Endpoints Protegidos (Requerem Token)
GET /auth/me: Retorna os dados do usuário logado.

POST /restaurantes: Cadastra um novo restaurante (ADMIN).

PUT /restaurantes/{id}: Atualiza um restaurante (ADMIN ou RESTAURANTE dono).

POST /produtos: Cadastra um novo produto (ADMIN ou RESTAURANTE dono).

PUT /produtos/{id}: Atualiza um produto (ADMIN ou RESTAURANTE dono).

DELETE /produtos/{id}: Remove um produto (ADMIN ou RESTAURANTE dono).

POST /pedidos: Cria um novo pedido (CLIENTE).

GET /pedidos/{id}: Busca um pedido (ADMIN ou envolvidos no pedido).

GET /pedidos/cliente/{clienteId}: Histórico de pedidos do cliente (ADMIN ou o próprio CLIENTE).

GET /pedidos/restaurante/{restauranteId}: Pedidos recebidos pelo restaurante (ADMIN ou o próprio RESTAURANTE).

PATCH /pedidos/{id}/status: Atualiza o status de um pedido.

GET /relatorios/...: Endpoints de relatórios (ADMIN ou RESTAURANTE dono).

(... e outros endpoints de CRUD e gerenciamento.)

🌟 Padronização de Respostas
Sucesso (2xx) e Paginação
Respostas de sucesso seguem um wrapper padrão (ApiResponseWrapper) e as respostas paginadas (PagedResponseWrapper) incluem metadados de paginação.

JSON

{
  "success": true,
  "data": { ... },
  "message": "Operação realizada com sucesso",
  "timestamp": "2025-10-21T12:00:00Z"
}
Erros (4xx / 5xx)
Erros de validação, autenticação e autorização seguem um padrão RFC 7807 (ErrorResponse).

Erro 401 (Unauthorized) - (Token ausente, inválido ou expirado)

JSON

{
  "status": 401,
  "error": "Unauthorized",
  "message": "Token expirado",
  "path": "/api/pedidos/1"
}
Erro 403 (Forbidden) - (Usuário não tem permissão)

JSON

{
  "status": 403,
  "error": "Forbidden",
  "message": "Acesso negado",
  "path": "/api/restaurantes"
}
Erro 400 (Bad Request) - (Validação de DTO)

JSON

{
  "timestamp": "2025-10-21T12:00:00",
  "status": 400,
  "error": "Dados inválidos",
  "message": "Erro de validação nos dados enviados",
  "path": "/api/produtos",
  "details": {
    "nome": "Nome é obrigatório"
  }
}
🔧 Como Executar
Clonar o repositório:

Bash

git clone https://github.com/DimasRabelo/delivery-api.git
cd delivery-api
Executar a aplicação (via Maven Wrapper):

Bash

./mvnw spring-boot:run
A API estará disponível em http://localhost:8080.

🧪 Como Testar (Autenticação)
Registre um usuário: POST http://localhost:8080/api/auth/register (Envie um JSON com nome, email, senha e role - ex: "CLIENTE").

Faça Login: POST http://localhost:8080/api/auth/login (Envie email e senha).

Copie o Token: A resposta irá conter o token (ex: "eyJhbGciOi...").

Teste Endpoints Protegidos: Para acessar endpoints como GET /api/auth/me, configure sua ferramenta (Postman/Insomnia) para incluir o Bearer Token no Header de Autorização: Authorization: Bearer eyJhbGciOi...

🌐 Links Úteis
Swagger UI (Documentação Interativa): http://localhost:8080/swagger-ui/index.html

API Docs (JSON OpenAPI): http://localhost:8080/api-docs

H2 Database Console (Acesso ao banco): http://localhost:8080/h2-console

JDBC URL: jdbc:h2:mem:deliverydb

User: sa

Password: password (definido em application.properties)

👨‍💻 Desenvolvedor
Dimas Aparecido Rabelo

🎓 Curso: Arquitetura de Sistemas

💻 Tecnologias: Java 21 | Spring Boot | Spring Security | JWT | H2 | Maven

📍 Projeto desenvolvido para módulos de API REST, Serviços e Segurança.
