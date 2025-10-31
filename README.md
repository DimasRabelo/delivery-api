<h1>🍔 DeliveryTech API </h1>

Sistema de delivery robusto desenvolvido com Spring Boot 3 e Java 21, focado em alta performance, segurança e excelente experiência para o desenvolvedor (DX).

Este projeto implementa uma API REST completa para gerenciar clientes, restaurantes, produtos e pedidos, com uma camada de segurança granular usando Spring Security 6 e autenticação stateless via JSON Web Tokens (JWT).

O sistema controla o acesso baseado em perfis (ADMIN, RESTAURANTE, CLIENTE) e expõe uma documentação interativa profissional usando Swagger (OpenAPI), permitindo que equipes de front-end e parceiros integrem com a API em questão de horas, não semanas.

<h2>🚀 Tecnologias Utilizadas</h2>

Java 21 LTS

Spring Boot 3.5.6

Spring Web: Para construção de endpoints REST.

Spring Data JPA: Para persistência de dados (com Hibernate).

Spring Validation: Para validação de DTOs.

Spring Security 6: Para Autenticação e Autorização.

JWT (JSON Web Tokens): Para gerenciamento de sessão stateless (via biblioteca jjwt).

H2 Database: Banco de dados relacional em memória para desenvolvimento e testes.

springdoc-openapi (Swagger): Para documentação interativa da API (v2.8.0).

ModelMapper: Para conversão simplificada entre Entidades e DTOs.

Maven: Para gerenciamento de dependências.

JUnit 5 & Mockito: Para testes unitários e de integração.

JaCoCo: Para relatórios de cobertura de testes.

<h2>📖 Documentação Interativa (Swagger)</h2>

A forma mais fácil e rápida de entender, testar e integrar com esta API é usando a interface interativa do Swagger, que foi o foco da Atividade 4.

Após iniciar a aplicação, acesse os links:

Interface Gráfica (Swagger UI):

Navegue visualmente por todos os endpoints, veja os schemas (modelos) de dados e teste as rotas diretamente do seu navegador.

Definição JSON (OpenAPI):

O arquivo JSON que descreve toda a API. Use-o para gerar clientes de API automaticamente em outras linguagens (TypeScript, Dart, etc.).

Como usar a Autenticação no Swagger

Muitos endpoints (marcados com o cadeado 🔒) requerem um token JWT para funcionar.

Vá até a seção 1. Autenticação na interface do Swagger.

Use o endpoint POST /api/auth/login (com um usuário válido do data.sql, ex: joao@email.com / senha123).

Copie o token JWT da resposta.

Clique no botão "Authorize" no topo direito da página.

Na janela que abrir, cole seu token no campo "Value" (o prefixo Bearer já deve estar lá, se não estiver, adicione) e clique em "Authorize".

Pronto! Todos os seus testes seguintes no Swagger UI estarão autenticados.

🔧 Como Executar (Ambiente de Desenvolvimento)

1. Clonar o repositório:

2. Executar a aplicação (via Maven Wrapper):

A API estará disponível em http://localhost:8080.

Links Úteis (Ambiente Local)

API Base URL: http://localhost:8080

Swagger UI (Documentação): http://localhost:8080/swagger-ui.html

H2 Database Console: http://localhost:8080/h2-console

JDBC URL: jdbc:h2:mem:deliverydb

User: sa

Password: (em branco) (definido em application.properties)

<h2>🏗️ Arquitetura</h2>

A aplicação segue uma arquitetura em camadas, agora com o JwtAuthenticationFilter como o "portão de entrada" para requisições protegidas.

<h2>🏗️ Estrutura de Pastas</h2>
A estrutura do projeto foi organizada para refletir a separação de responsabilidades, com um novo pacote security dedicado:

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
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜UserResponse.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜UsuarioUpdateDTO.java
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
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜RestauranteServiceImpl.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜UsuarioServiceImpl.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ClienteService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜PedidoService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ProdutoService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜RelatorioService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜RestauranteService.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📂validation
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜CEPValidator.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜CategoriaValidator.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜CpfValidator.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜HorarioFuncionamentoValidator.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜TelefoneValidator.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ValidCEP.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ValidCategoria.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ValidHorarioFuncionamento.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜ValidTelefone.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📜DeliveryApiApplication.java
 ┃ ┃ ┃ ┃ ┃ ┗ 📜GerarSenha.java
 ┃ ┗ 📂resources
 ┃ ┃ ┣ 📜_data.sql
 ┃ ┃ ┣ 📜application.properties
 ┃ ┃ ┗ 📜data.sql
 ┣ 📂postman
 ┃ ┣ 📜DeliveryApi.postman_collection.json
 ┃ ┣ 📜DeliveryApiLogin.postman_collection.json
 ┃ ┣ 📜DeliveryApiTestValidation.postman_collection.json
 ┃ ┗ 📜Relatorios Delivery API.postman_collection.json
 ┗ 📂test
 ┃ ┣ 📂java
 ┃ ┃ ┗ 📂com
 ┃ ┃ ┃ ┗ 📂deliverytech
 ┃ ┃ ┃ ┃ ┗ 📂delivery
 ┃ ┃ ┃ ┃ ┃ ┣ 📂config
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜TestDataConfiguration.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📂controller
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂auth
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜AuthControllerIntegrationTest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ClienteControllerIntegrationTest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜PedidoControllerIntegrationTest.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📂entity
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ClienteTest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ProdutoTest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜RestauranteTest.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📂integration
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜SwaggerIntegrationTest.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📂security
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📂jwt
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜JwtAuthenticationFilterTest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜JwtUtilTest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜SecurityUtilsTest.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📂service
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ClienteServiceTest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜PedidoServiceTest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ProdutoServiceTest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜RestauranteServiceTest.java
 ┃ ┃ ┃ ┃ ┃ ┗ 📂validation
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜CpfValidatorTest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜HorarioFuncionamentoValidatorTest.java
 ┃ ┗ 📂resources
 ┃ ┃ ┗ 📜application-test.properties

<h2>⚙️ Funcionalidades Implementadas</h2>

<h2>🔐 Segurança (Spring Security + JWT)</h2>

Autenticação Stateless: Autenticação via Bearer Token (JWT).

Autorização Granular: Uso de @PreAuthorize para controle de acesso em nível de método, diferenciando ADMIN, RESTAURANTE e CLIENTE.

Verificação de Propriedade: Lógica de serviço (ex: @produtoService.isOwner(#id)) que garante que um usuário RESTAURANTE só possa editar seus próprios recursos.

Endpoints de Autenticação: POST /api/auth/login, POST /api/auth/register e GET /api/auth/me.

Hashing de Senhas: Senhas são armazenadas usando BCryptPasswordEncoder.

Tratamento de Exceções: Respostas 401 (Unauthorized) e 403 (Forbidden) customizadas e padronizadas.

<h2>🛠️ Services (Regras de Negócio)</h2>

AuthService: Implementa UserDetailsService para carregar usuários e gerencia o registro.

RestauranteService: Cadastro, filtros, cálculo de taxa de entrega e verificação de propriedade (isOwner).

ProdutoService: Gerenciamento de cardápio e verificação de propriedade (isOwner).

PedidoService: Lógica complexa para criação de pedidos, cálculo de total, atualização de status e verificação de acesso (canAccess).

RelatorioService: Geração de relatórios de vendas, produtos, clientes, etc.

<h2>📦 DTOs e Validações</h2>

Auth DTOs: LoginRequest, LoginResponse (com token), RegisterRequest, UserResponse (DTO seguro, sem senha).

Request DTOs: ClienteDTO, RestauranteDTO, ProdutoDTO, PedidoDTO, ItemPedidoDTO.

Response DTOs: ClienteResponseDTO, RestauranteResponseDTO, ProdutoResponseDTO, PedidoResponseDTO, e wrappers de resposta (ApiResponseWrapper, PagedResponseWrapper).

Validações: @Valid, @NotNull, @NotBlank, @Email, @Size, e validações customizadas.

<h2>📋 Endpoints REST (Principais)</h2>

A API é dividida em endpoints públicos (para consulta) e protegidos (que exigem autenticação e autorização). Para uma lista completa e interativa, acesse o .

Base URL: http://localhost:8080/api

<h2>🔑 Autenticação (Público)</h2>

POST /auth/login: Autentica um usuário e retorna um token JWT.

POST /auth/register: Registra um novo usuário (CLIENTE ou RESTAURANTE).

<h2>🍽️ Endpoints Públicos (Consulta)</h2>

GET /restaurantes: Lista restaurantes (com filtros).

GET /restaurantes/{id}: Busca um restaurante por ID.

GET /restaurantes/{id}/produtos: Lista o cardápio (produtos) de um restaurante.

GET /produtos/{id}: Busca um produto por ID.

POST /pedidos/calcular: Calcula o total de um pedido (sem salvar).

<h2>🛡️ Endpoints Protegidos (Requerem Token)</h2>

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

GET /relatorios/...: Endpoints de relatórios (ADMIN ou RESTAURANTE dono). (... e outros endpoints de CRUD e gerenciamento.)

<h2>🌟 Padronização de Respostas</h2>

Sucesso (2xx) e Paginação

Respostas de sucesso seguem um wrapper padrão (ApiResponseWrapper) e as respostas paginadas (PagedResponseWrapper) incluem metadados de paginação.

Erros (4xx / 5xx)
Erros de validação, autenticação e autorização seguem um padrão (ErrorResponse).

Erro 401 (Unauthorized) - (Token ausente, inválido ou expirado)

Erro 403 (Forbidden) - (Usuário não tem permissão)

Erro 400 (Bad Request) - (Validação de DTO)

<h2>🧪 Testes Automatizados</h2>

Este projeto possui uma suíte robusta de testes automatizados (107 testes no total) para garantir a qualidade e estabilidade do código, cobrindo:

Testes Unitários (Services): Verificam as regras de negócio de forma isolada (ClienteServiceTest, PedidoServiceTest, etc.).

Testes de Integração (Controllers): Verificam a API de ponta a ponta, simulando requisições HTTP (ClienteControllerIntegrationTest, etc.).

Testes de Documentação: Verificam se a documentação Swagger está sendo gerada corretamente (SwaggerIntegrationTest).

Como Executar os Testes
1. Executar toda a suíte de testes (100+ testes): Este comando limpa o projeto, executa todos os testes unitários e de integração.

(Se você estiver usando os scripts da atividade, pode usar ./run-all-tests.sh)

2. Executar um grupo de testes específico (Scripts):

<h2>📊 Relatório de Cobertura de Código (JaCoCo)</h2>

O projeto está configurado com o JaCoCo para monitorar a cobertura dos testes.

1. Gere o relatório:

2. Abra o relatório no seu navegador: O relatório estará em target/site/jacoco/index.html.

(Os scripts run-all-tests.sh e run-unit-tests.sh já devem gerar o relatório automaticamente)

👨‍💻 Desenvolvedor
Dimas Aparecido Rabelo

🎓 Curso: Arquitetura de Sistemas 💻 Tecnologias: Java 21 | Spring Boot | Spring Security | JWT | H2 | Maven | Swagger 📍 Projeto desenvolvido para módulos de API REST, Serviços e Segurança.
