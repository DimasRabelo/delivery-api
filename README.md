🍔 DeliveryTech API

Sistema de delivery desenvolvido com Spring Boot e Java 21 LTS para gerenciar clientes, restaurantes, produtos e pedidos.
Agora com camada de serviços e controllers REST completos, regras de negócio robustas e transações consistentes.

🚀 Tecnologias Utilizadas

Java 21 LTS

Spring Boot 3.5.6

Spring Web, Spring Data JPA

H2 Database (em memória)

Maven

Bean Validation

ModelMapper

🏗️ Arquitetura do Sistema
[Cliente Mobile/Web]
        ↓ HTTP REST
[Controllers] ← Recebem requisições, validam entrada
        ↓
[Services] ← Regras de negócio, validações, transações
        ↓
[Repositories] ← Acesso aos dados
        ↓
[Banco de Dados]


Controllers: Exposição de endpoints REST

Services: Lógica de negócio, validações, transações

Repositories: Acesso e consultas ao banco

Banco: H2 em memória para testes

🧩 Estrutura das Pastas
src/main/java
 └─ com.deliverytech
     ├─ controller       # REST Controllers
     ├─ service          # Lógica de negócio
     ├─ repository       # Camada de persistência
     ├─ dto              # DTOs request/response
     └─ entity           # Entidades JPA

⚙️ Funcionalidades Implementadas
🛠️ Camada de Services

ClienteService: cadastro, busca, atualização, toggle de status

RestauranteService: cadastro, busca, filtro por categoria, taxa de entrega

ProdutoService: cadastro, busca por restaurante/categoria, controle de disponibilidade

PedidoService: criação de pedido com transação completa, cálculo de total, atualização de status, cancelamento

📦 DTOs e Validações

Request DTOs: ClienteDTO, RestauranteDTO, ProdutoDTO, PedidoDTO, ItemPedidoDTO

Response DTOs: ClienteResponseDTO, RestauranteResponseDTO, ProdutoResponseDTO, PedidoResponseDTO, PedidoResumoDTO

Validações: @NotNull, @NotBlank, @Email, @Size, @DecimalMin, @Valid

🔄 Regras de Negócio

Cliente ativo obrigatoriamente para pedidos

Email único para clientes

Produtos válidos pertencentes ao restaurante

Status de pedido com transições válidas

Total do pedido calculado somando itens + taxa de entrega

💥 Transações

@Transactional nos métodos críticos (ex.: criarPedido)

Falha em qualquer etapa reverte toda a operação

📋 Endpoints REST
🔹 Clientes

POST /api/clientes → Cadastrar cliente

GET /api/clientes → Listar clientes ativos

GET /api/clientes/{id} → Buscar cliente por ID

GET /api/clientes/email/{email} → Buscar cliente por email

PUT /api/clientes/{id} → Atualizar cliente

PATCH /api/clientes/{id}/status → Ativar/Desativar cliente

🔹 Restaurantes

POST /api/restaurantes → Cadastrar restaurante

GET /api/restaurantes → Listar restaurantes ativos

GET /api/restaurantes/{id} → Buscar restaurante por ID

GET /api/restaurantes/categoria/{categoria} → Filtrar por categoria

PUT /api/restaurantes/{id} → Atualizar restaurante

GET /api/restaurantes/{id}/taxa-entrega/{cep} → Calcular taxa de entrega

🔹 Produtos

POST /api/produtos → Cadastrar produto

GET /api/produtos/{id} → Buscar produto por ID

GET /api/restaurantes/{restauranteId}/produtos → Produtos do restaurante

PUT /api/produtos/{id} → Atualizar produto

PATCH /api/produtos/{id}/disponibilidade → Alterar disponibilidade

GET /api/produtos/categoria/{categoria} → Filtrar por categoria

🔹 Pedidos

POST /api/pedidos → Criar pedido (transação completa)

POST /api/pedidos/calcular → Calcular total sem salvar

GET /api/pedidos/{id} → Consultar pedido completo

GET /api/clientes/{clienteId}/pedidos → Histórico do cliente

PATCH /api/pedidos/{id}/status → Atualizar status do pedido

DELETE /api/pedidos/{id} → Cancelar pedido

🧪 Testes e Validação

Testes unitários para Services

Testes de integração para Controllers

Collection Postman/Insomnia: postman/DeliveryAPI.postman_collection.json

🏃‍♂️ Como Executar
git clone https://github.com/DimasRabelo/delivery-api.git
cd delivery-api
./mvnw spring-boot:run


Console H2: http://localhost:8080/h2-console

JDBC URL: jdbc:h2:mem:deliverydb

User: sa

Password: (em branco)

👨‍💻 Desenvolvedor

Dimas Aparecido Rabelo
🎓 Curso: Arquitetura de Sistemas
💻 Tecnologias: Java 21 | Spring Boot | H2 | Maven
📍 Projeto desenvolvido para módulo de Camada de Serviços e REST API
