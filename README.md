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

📋 Endpoints REST (Testes Locais)

Todos os endpoints devem ser testados em: http://localhost:8080/

Você pode testar todos os endpoints usando a coleção Postman:
DeliveryTech Postman Collection

🔹 Clientes

Criar Cliente

POST /api/clientes
Content-Type: application/json

{
  "nome": "Lucas Pereira",
  "email": "lucas@email.com",
  "telefone": "(11) 99999-7777",
  "endereco": "Rua F, 404 - São Paulo/SP"
}


Listar Clientes Ativos

GET /api/clientes


Buscar Cliente por ID

GET /api/clientes/4


Buscar Cliente por Email

GET /api/clientes/email/pedro@email.com


Atualizar Cliente

PUT /api/clientes/1
Content-Type: application/json

{
  "id": 1,
  "nome": "João Silva",
  "email": "joao@email.com",
  "telefone": "(11) 99999-1111",
  "endereco": "Rua Antonio Pinto Ferreira Filho, 123 - São Paulo/SP",
  "ativo": true
}


Ativar/Desativar Cliente

PATCH /api/clientes/1/status
Content-Type: application/json

{
  "ativo": false
}

🔹 Restaurantes

Cadastrar Restaurante

POST /api/restaurantes
Content-Type: application/json

{
  "nome": "Churrascaria Bom Sabor",
  "categoria": "Churrasco",
  "endereco": "Av. Brasil, 123",
  "telefone": "(11) 4444-5555",
  "taxaEntrega": 6.5,
  "avaliacao": 4.3,
  "ativo": true
}


Listar Restaurantes Ativos

GET /api/restaurantes


Buscar Restaurante por ID

GET /api/restaurantes/3


Filtrar por Categoria

GET /api/restaurantes/categoria/Hamburgueria


Atualizar Restaurante

PUT /api/restaurantes/2
Content-Type: application/json

{
  "nome": "Churrascaria Bom Sabor",
  "categoria": "Churrasco",
  "endereco": "Av. Brasil, 800",
  "telefone": "(11) 4444-5555",
  "taxaEntrega": 6.5,
  "avaliacao": 4.3
}


Calcular Taxa de Entrega

GET /api/restaurantes/4/taxa-entrega/01001-000


Ativar/Desativar Restaurante

PATCH /api/restaurantes/4/status
Content-Type: application/json

{
  "ativo": true
}

🔹 Produtos

Cadastrar Produto

POST /api/produtos
Content-Type: application/json

{
  "nome": "Pizza Quatro Queijos",
  "descricao": "Molho de tomate, mussarela, parmesão, provolone e gorgonzola",
  "categoria": "Pizza",
  "preco": 42.90,
  "disponivel": true,
  "restauranteId": 1
}


Listar Produtos de um Restaurante

GET /api/produtos/restaurante/2


Buscar Produto por ID

GET /api/produtos/3


Filtrar Produto por Categoria

GET /api/produtos/categoria/Pizza


Alterar Disponibilidade

PATCH /api/produtos/1/disponibilidade?disponivel=false


Atualizar Produto

PUT /api/produtos/1
Content-Type: application/json

{
  "nome": "Pizza Margherita Atualizada",
  "descricao": "Molho de tomate, mussarela, manjericão e orégano",
  "preco": 42.00,
  "categoria": "Pizza",
  "disponivel": true,
  "restauranteId": 1
}

🔹 Pedidos

Criar Pedido

POST /api/pedidos
Content-Type: application/json

{
  "clienteId": 1,
  "restauranteId": 1,
  "itens": [
    { "produtoId": 2, "quantidade": 1 },
    { "produtoId": 3, "quantidade": 1 }
  ],
  "observacoes": "Sem cebola",
  "enderecoEntrega": "Rua Exemplo, 123"
}


Calcular Total dos Itens

POST /api/pedidos/calcular
Content-Type: application/json

[
  { "produtoId": 2, "quantidade": 2 },
  { "produtoId": 3, "quantidade": 1 }
]


Atualizar Status do Pedido

PATCH /api/pedidos/2/status
Content-Type: application/json

{
  "status": "PREPARANDO"
}


Listar Histórico de Pedidos de um Cliente

GET /api/pedidos/clientes/3/pedidos


Cancelar Pedido

DELETE /api/pedidos/1

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
