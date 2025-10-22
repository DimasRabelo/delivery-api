🍔 DeliveryTech API

Sistema de delivery desenvolvido com Spring Boot 3.5.6 e Java 21 LTS para gerenciar clientes, restaurantes, produtos, pedidos e relatórios.
Agora com camada de serviços robusta, controllers REST completos, validações, transações consistentes e documentação profissional com Swagger/OpenAPI.

🚀 Tecnologias Utilizadas

Java 21 LTS

Spring Boot 3.5.6 (Web, Data JPA, Validation)

H2 Database (em memória)

Maven

ModelMapper

springdoc-openapi-ui (Swagger)

🏗️ Arquitetura
[App Mobile / Portal Web / Integrações]
        ↓ HTTP REST
[Controllers] ← Recebem requisições, validam entrada
        ↓
[Services] ← Regras de negócio e transações
        ↓
[Repositories] ← Acesso ao banco de dados
        ↓
[Banco de Dados (H2)]


Controllers → Endpoints REST

Services → Lógica de negócio, validações e transações

Repositories → Persistência

Banco → H2 em memória

🧩 Estrutura de Pastas

![Estrutura do projeto](https://raw.githubusercontent.com/DimasRabelo/delivery-api/main/src/main/estrutura%3Dprojeto.png)


⚙️ Funcionalidades Implementadas
🛠️ Services

ClienteService → cadastro, busca, atualização, toggle status

RestauranteService → cadastro, filtros, cálculo taxa entrega

ProdutoService → cadastro, busca, controle de disponibilidade

PedidoService → criação de pedidos, cálculo de total, atualização de status, cancelamento

RelatorioService → geração de relatórios: vendas, produtos mais vendidos, clientes ativos, pedidos por período

📦 DTOs e Validações

Request DTOs: ClienteDTO, RestauranteDTO, ProdutoDTO, PedidoDTO, ItemPedidoDTO

Response DTOs: ClienteResponseDTO, RestauranteResponseDTO, ProdutoResponseDTO, PedidoResponseDTO, PedidoResumoDTO, RelatorioVendasDTO, RelatorioProdutosDTO, RelatorioClientesDTO, RelatorioPedidosDTO

Validações padrão: @NotNull, @NotBlank, @Email, @Size, @DecimalMin, @Valid

Validações customizadas: @ValidCEP, @ValidTelefone, @ValidCategoria, @ValidHorarioFuncionamento

🔄 Regras de Negócio

Cliente deve estar ativo para pedidos

Email único para clientes

Produtos válidos pertencentes ao restaurante

Status de pedido com transições válidas

Total do pedido = soma itens + taxa de entrega

💥 Transações

@Transactional em métodos críticos

Falha em qualquer etapa → operação revertida

📋 Endpoints REST

Base URL: http://localhost:8080/api

🔹 Clientes

POST /clientes → criar cliente

GET /clientes → listar clientes ativos

GET /clientes/{id} → buscar cliente por ID

GET /clientes/email/{email} → buscar por email

PUT /clientes/{id} → atualizar cliente

PATCH /clientes/{id}/status → ativar/desativar

🔹 Restaurantes

CRUD completo

Filtros: por categoria e ativo

Cálculo de taxa de entrega: /restaurantes/{id}/taxa-entrega/{cep}

Restaurantes próximos: /restaurantes/proximos/{cep}

🔹 Produtos

CRUD completo

Toggle disponibilidade: /produtos/{id}/disponibilidade

Filtros: por restaurante, categoria ou nome

🔹 Pedidos

Criar, buscar, atualizar status e cancelar

Histórico por cliente e restaurante

Calcular total sem salvar: /pedidos/calcular

🔹 Relatórios

Vendas por restaurante: /relatorios/vendas-por-restaurante?dataInicio=YYYY-MM-DD&dataFim=YYYY-MM-DD

Produtos mais vendidos: /relatorios/produtos-mais-vendidos?dataInicio=YYYY-MM-DD&dataFim=YYYY-MM-DD

Clientes mais ativos: /relatorios/clientes-ativos?dataInicio=YYYY-MM-DD&dataFim=YYYY-MM-DD

Pedidos por período: /relatorios/pedidos-por-periodo?dataInicio=YYYY-MM-DD&dataFim=YYYY-MM-DD

Todos os relatórios retornam ApiResponse<T> padronizado.

🧪 Testes

MockMvc → integração completa para todos os controllers

Cenários obrigatórios: criação, busca, atualização, exclusão, filtros, relatórios

Testes de validação: dados inválidos, conflitos, entidades inexistentes

Collection Postman/Insomnia pronta para execução

🎯 Cenários de Teste Obrigatórios

GET /restaurantes?categoria=Italiana&ativo=true&page=0&size=10

GET /restaurantes/1/produtos?disponivel=true

POST /pedidos → criar pedido completo

GET /relatorios/vendas-por-restaurante → período definido

Swagger UI → interface funcionando, Try it Out

🌟 Padronização de Respostas

ApiResponse<T>:

{
  "success": true,
  "data": { ... },
  "message": "Operação realizada com sucesso",
  "timestamp": "2025-10-21T12:00:00Z"
}


PagedResponse<T>:

{
  "content": [ ... ],
  "page": { "number":0, "size":10, "totalElements":50, "totalPages":5 },
  "links": { "first":"/api/restaurantes?page=0", "last":"/api/restaurantes?page=4" }
}


ErrorResponse (RFC 7807):

{
  "timestamp": "2025-10-21T12:00:00",
  "status": 400,
  "error": "Dados inválidos",
  "message": "Erro de validação nos dados enviados",
  "path": "/api/produtos",
  "details": { "nome": "Nome é obrigatório", "preco": "Preço deve ser maior que zero" }
}

🔧 Como Executar
git clone https://github.com/DimasRabelo/delivery-api.git
cd delivery-api
./mvnw spring-boot:run


Swagger UI: http://localhost:8080/swagger-ui/index.html

API Docs: http://localhost:8080/api-docs

H2 Console: http://localhost:8080/h2-console

JDBC URL: jdbc:h2:mem:deliverydb

User: sa

Password: (em branco)

📦 Entregáveis

Controllers REST completos

Swagger/OpenAPI atualizado

Testes de integração (MockMvc)

Respostas padronizadas e códigos HTTP corretos

Collection Postman/Insomnia com todos os cenários

👨‍💻 Desenvolvedor

Dimas Aparecido Rabelo
🎓 Curso: Arquitetura de Sistemas
💻 Tecnologias: Java 21 | Spring Boot | H2 | Maven
📍 Projeto desenvolvido para módulo de Camada de Serviços e REST API
