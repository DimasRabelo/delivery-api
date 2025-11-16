<h1>🍔 DeliveryTech API 🔬</h1>

Sistema de delivery robusto desenvolvido com Spring Boot 3 e Java 21, focado em alta performance (com **caching distribuído via Redis**), segurança, observabilidade e excelente experiência para o desenvolvedor (DX).

Este projeto implementa uma API REST completa para gerenciar clientes, restaurantes, produtos e pedidos, com uma camada de segurança granular usando Spring Security 6 e autenticação stateless via JSON Web Tokens (JWT).

O sistema agora inclui um conjunto completo de ferramentas de Observabilidade, incluindo:

* Health Checks customizados via Spring Boot Actuator.
* Métricas de Negócio (ex: pedidos, receita) via Micrometer e Prometheus.
* Logging Estruturado (JSON) com Correlation IDs para rastreabilidade.
* Distributed Tracing (Micrometer Tracing) para monitoramento de performance.
* Um Dashboard em tempo real para visualização das métricas.

<h2>🚀 Tecnologias Utilizadas</h2>

* **Java 21 LTS**
* **Spring Boot 3.5.6**
* **Spring Web:** Para endpoints REST.
* **Spring Data JPA:** Para persistência de dados (com Hibernate).
* **Spring Validation:** Para validação de DTOs.
* **Spring Security 6:** Para Autenticação e Autorização.
* **Spring Cache:** (Novo) Para abstração de cache (`@Cacheable`, `@CacheEvict`).
* **Redis:** (Novo) Cache distribuído para performance em escala.
* **JWT (JSON Web Tokens):** Para gerenciamento de sessão stateless (via `jjwt`).
* **H2 Database:** Banco de dados em memória para desenvolvimento e testes.
* **springdoc-openapi (Swagger):** Para documentação interativa da API.
* **ModelMapper:** Para conversão entre Entidades e DTOs.
* **Maven:** Para gerenciamento de dependências.
* **JUnit 5 & Mockito:** Para testes unitários e de integração.
* **JaCoCo:** Para relatórios de cobertura de testes.

<h2>✨ Novas Tecnologias (Atividade de Observabilidade) ✨</h2>

* **Spring Boot Actuator:** Expõe endpoints de gerenciamento (`/health`, `/info`, `/metrics`, `/prometheus`).
* **Micrometer (Core, Tracing & Prometheus):** Coleta métricas de performance (JVM, CPU), métricas de negócio customizadas (pedidos, receita) e gera traces (substituto moderno do Sleuth).
* **Logback (Customizado):** Configurado para gerar logs estruturados (JSON), logs de auditoria separados e incluir CorrelationID e TraceID em todas as saídas.
* **Thymeleaf:** Motor de template usado para renderizar o Dashboard de monitoramento.

<h2>🛰️ Distributed Tracing com Zipkin</h2>

O sistema agora possui rastreamento distribuído completo utilizando o **Micrometer Tracing** integrado com o **Zipkin**, permitindo visualizar todo o fluxo de requisições entre serviços, identificar gargalos de performance e otimizar o tempo de resposta da aplicação.

<h3>🔧 Como funciona</h3>

A API já está configurada com:
<ul>
  <li><strong>Micrometer Tracing</strong> — responsável por capturar eventos (spans e traces)</li>
  <li><strong>Bridge Brave</strong> — envia esses dados para o servidor Zipkin</li>
  <li><strong>Logging com TraceID e SpanID</strong> — todos os logs agora incluem identificadores únicos para rastreamento</li>
</ul>

Cada requisição gera automaticamente:
<ul>
  <li><strong>TraceID</strong>: identifica a requisição completa</li>
  <li><strong>SpanID</strong>: identifica cada etapa dentro dessa requisição</li>
</ul>

<h2>📡 Serviço Zipkin (Docker Compose)</h2>

O Zipkin está disponível automaticamente quando você sobe o ambiente com Docker Compose.

Acesse pelo navegador:
```
http://localhost:9411
```
Lá você pode visualizar:
<ul>
  <li>Lista de traces recentes</li>
  <li>Tempo de resposta por requisição</li>
  <li>Árvore de spans (timeline)</li>
  <li>Dependências entre serviços</li>
  <li>Gargalos de latência</li>
</ul>

<h2>📝 Configurações utilizadas</h2>

No <code>application.properties</code> / <code>.yml</code> dentro do perfil <strong>docker</strong>:

<pre>
management.tracing.sampling.probability=1.0
management.zipkin.tracing.endpoint=http://zipkin:9411/api/v2/spans
</pre>

<h2>📊 Exemplo de logs com TraceID</h2>

<pre>
INFO  [delivery-api,traceId=bd12f93c1f2a3e77,spanId=5a9c12b1f7d9c1a3] [CorrelationID=8f4e1b2c7d] Pedido criado com sucesso
</pre>

Agora você consegue identificar exatamente:
<ul>
  <li>qual requisição gerou o log</li>
  <li>qual fluxo ela percorreu</li>
  <li>onde ocorreu o gargalo</li>
</ul>

<h2>🚀 Benefícios do Zipkin no projeto</h2>

<ul>
  <li>Rastreamento ponta-a-ponta de requisições</li>
  <li>Detecção fácil de problemas de performance</li>
  <li>Visão clara do tempo gasto em cada camada</li>
  <li>Integração automática com Micrometer</li>
  <li>Logs e métricas conectados pelo mesmo TraceID</li>
</ul>

<p>Com isso, o DeliveryTech agora conta com um ambiente de Observabilidade completo (Logs + Métricas + Traces), alinhado com os padrões modernos usados em sistemas distribuídos.</p>

<h2>✨ Novas Tecnologias (Performance & Cache) ✨</h2>

Para resolver a latência em consultas repetidas ao banco de dados, uma camada de cache distribuído foi implementada:

* **Spring Cache Abstraction:** Habilitação do cache via `@EnableCaching`.
* **Cache Distribuído com Redis:** Configurado para ser o provedor de cache padrão, garantindo consistência de dados entre múltiplas instâncias da API.
* **`@Cacheable`:** Aplicado em métodos de leitura frequente (como `ProdutoService.buscarProdutoPorId`) para reduzir drasticamente o acesso ao banco de dados.
* **`@CacheEvict`:** Aplicado em métodos de escrita (`atualizarProduto`, `removerProduto`) para invalidar o cache e prevenir dados desatualizados (*stale data*).
* **Serialização:** DTOs de resposta (como `ProdutoResponseDTO`) foram atualizados para implementar `Serializable`, permitindo o armazenamento e transporte para o Redis.

<h2>📖 Documentação Interativa (Swagger)</h2>

A forma mais fácil e rápida de entender, testar e integrar com esta API é usando a interface interativa do Swagger.

Após iniciar a aplicação, acesse os links:

* **Interface Gráfica (Swagger UI):** `http://localhost:8080/swagger-ui.html`
* **Definição JSON (OpenAPI):** `http://localhost:8080/api-docs`

(O seu texto sobre "Como usar a Autenticação no Swagger" estava perfeito e foi mantido)

<h2>🔬 Observabilidade e Monitoramento</h2>

O projeto agora possui um conjunto completo de ferramentas de monitoramento.

### 1. Dashboard Interativo (Front-end)

* **URL do Dashboard:** **`http://localhost:8080/dashboard`**

![Print do Dashboard de Monitoramento](https://raw.githubusercontent.com/DimasRabelo/imagens/main/dashboard-monitoramento.png)

Um dashboard em tempo real (atualizado a cada 5 segundos) foi criado para visualizar as métricas de negócio e performance.

### 2. Endpoints do Actuator (Back-end)

Os endpoints do Actuator fornecem os dados brutos de saúde e métricas. (Nota: Estes endpoints (exceto /health) estão protegidos e requerem um token de ADMIN para acesso).

* **Saúde (Health Check):** `http://localhost:8080/actuator/health` (Público)
    * Verifica o status do banco de dados (H2) e de serviços externos (simulados).
* **Informações da Aplicação:** `http://localhost:8080/actuator/info` (Requer ADMIN)
    * Mostra informações do build, versão e o último commit do Git.
* **Métricas (Formato Prometheus):** `http://localhost:8080/actuator/prometheus` (Requer ADMIN)
    * Expõe todas as métricas (JVM, CPU, e as nossas customizadas como `delivery_pedidos_total`) para serem lidas por um servidor Prometheus.
* **Loggers (em tempo real):** `http://localhost:8080/actuator/loggers` (Requer ADMIN)
    * Permite visualizar e alterar os níveis de log (ex: de INFO para DEBUG) sem reiniciar a aplicação.

3. Logs Estruturados e Rastreamento
   
Logs Estruturados (JSON): Todos os logs da aplicação são salvos em logs/delivery-api-json.log.

Logs de Auditoria: Ações críticas (como a criação de pedidos) são salvas em logs/delivery-api-audit.log.

Rastreamento (TraceID + CorrelationID): Cada log de requisição no console agora inclui um TraceID (para o Zipkin) e um CorrelationID (para rastreamento), permitindo uma depuração completa: INFO [delivery-api,TraceID,SpanID] [CorrelationID] ... Mensagem de Log

<h2>🔧 Como Executar (Ambiente de Desenvolvimento)</h2>

```
mvn spring-boot:run
A API estará disponível em http://localhost:8080.
```

<h3>Links Úteis (Ambiente Local)</h3>

API Base URL: http://localhost:8080

Swagger UI (Documentação): http://localhost:8080/swagger-ui.html

H2 Database Console: http://localhost:8080/h2-console

JDBC URL: jdbc:h2:mem:deliverydb

User: sa

Password: 

Dashboard de Métricas: http://localhost:8080/dashboard

Endpoint de Saúde (Actuator): http://localhost:8080/actuator/health

<h2>🐳 Containerização Completa (Docker + Docker Compose)</h2>

Com o objetivo de padronizar ambientes e garantir que a aplicação seja executada de maneira idêntica em desenvolvimento, homologação e produção, este projeto agora inclui um Dockerfile otimizado e um ambiente completo orquestrado via Docker Compose.

É CRÍTICO que o repositório da API e este repositório do Frontend estejam no mesmo diretório de nível superior.
```
/seu_diretorio_de_projetos/
├── delivery-api/        <-- Contém o Docker Compose
└── delivery-frontend/   <-- ESTE REPOSITÓRIO (Contém o Dockerfile do React)
2. Clonagem e Inicialização

Siga estes passos para iniciar o ambiente multi-contêiner:

Clone os Repositórios: (Execute na pasta /seu_diretorio_de_projetos/):


# Execute na sua pasta raiz de projetos
git clone https://github.com/DimasRabelo/delivery-api.git
git clone https://github.com/DimasRabelo/delivery-frontend.git
```

<h2>📦 Dockerfile (Multi-Stage Build)</h2>

A aplicação foi empacotada utilizando multi-stage build, reduzindo drasticamente o tamanho final da imagem e garantindo melhor performance.

Principais otimizações:

Build isolado usando imagem Maven.

Execução final em imagem leve baseada em Alpine.

Jar otimizado e redução de camadas.

Variáveis de ambiente configuradas para o perfil docker.

Menor tempo de boot e menor uso de memória.

<h2>🧩 Orquestração com Docker Compose</h2>

O projeto inclui um docker-compose.yml que levanta:

API DeliveryTech

Banco de Dados MySQL

Redis (para cache distribuído)

Rede isolada e volumes persistentes

Isso permite replicar o ambiente real com apenas um comando.

<h2>🚀 Como subir o ambiente</h2>

Durante o desenvolvimento, utilizei o comando:

docker-compose up --build


Esse comando força o Docker a reconstruir as imagens, o que é útil quando você ainda está configurando o ambiente, alterando Dockerfile, ajustando frontend/backend, etc.

<h2>🧑‍💻 Para terceiros (usuários do projeto)</h2>

Se você está apenas baixando o projeto para utilizar, NÃO precisa usar --build.

Basta executar:

docker-compose up
```
E depois acessar: http://localhost
```
O Docker Compose irá subir automaticamente todos os serviços já configurados (API, frontend, banco, etc.), usando as imagens existentes ou montando o ambiente sem necessidade de rebuild completo.

<h2>📌 Serviços incluídos</h2>

API: http://localhost

MySQL: porta 3306 + volume persistente

Redis: porta 6379

Rede: delivery-network

<h2>🔑 Dados de Acesso Padrão (Senha: 123456)</h2>

O ambiente Docker é inicializado com os seguintes usuários para testes e desenvolvimento.

Inteligência de Login: Para todos os perfis, exceto Restaurante, a aplicação detecta automaticamente a Role após o login e redireciona o usuário para o painel correto (ADMIN, CLIENTE, ENTREGADOR). A autenticação do Restaurante é tratada em um endpoint/página separado para fins de segregação.

Nota: A senha padrão para todos os usuários listados abaixo é 123456 (armazenada via hash BCrypt).
```
| Usuário                        | E-mail                            Role |

| Administrador |             admin@delivery.com                   | ADMIN |
| Cliente|                     joao@email.com                      | CLIENTE |
| Cliente Secundário |        maria@email.com                      | CLIENTE |
| Restaurante Padrão |        pizza@palace.com                    | RESTAURANTE |
| Restaurante Secundário |     burger@king.com                    | RESTAURANTE |
| Entregador |                carlos@entrega.com                  | ENTREGADOR  |
```
⚠️ Observação: O Painel do Administrador (admin@delivery.com) ainda está em fase de implementação. Você conseguirá logar com sucesso, mas a página correspondente (a view) estará em branco ou incompleta.

<h2>✔️ Testes realizados</h2>

Comunicação API ↔ MySQL funcionando

Redis operando como provedor de cache

Migrações e inicialização OK

Persistência validada via volume

<h2>🔄 CI/CD – Pipeline Automatizado (GitHub Actions)</h2>

A aplicação agora conta com um pipeline CI/CD configurado para automatizar:

✔️ Build

Compila o projeto usando Maven e executa todos os testes.

✔️ Testes Automatizados

Executa testes unitários e de integração em cada push na branch principal.

✔️ Build da Imagem Docker

O pipeline gera automaticamente a imagem Docker da aplicação.

✔️ Deploy Automatizado

Em pushes para uma branch específica (ex: homolog), o pipeline:

Publica a imagem no Docker Hub (ou GitHub Registry)

Executa deploy em servidor de teste/homologação (SSH ou runner específico)

<h2>📝 Arquivo do Pipeline</h2>

O pipeline é configurado em:

.github/workflows/ci-cd.yml


Inclui as etapas:

checkout

setup-java

cache do Maven

mvn test

mvn -DskipTests package

docker build

(Opcional) docker push

(Opcional) deploy remoto

<h2>📊 Evidências</h2>

O repositório agora possui:

Prints do pipeline executando com sucesso

Logs completos de build, testes e deploy

Histórico de execuções disponível no GitHub Actions

<h2>🧠 Decisões Técnicas (Docker + CI/CD)</h2>

Multi-stage build reduz em até 70% o tamanho final da imagem.

Compose permite testar todo o ecossistema localmente.

CI/CD automatiza qualidade, reduz erros e acelera entregas.

Deploy automatizado garante versionamento e integridade das builds.

Redis + MySQL no Compose garantem ambiente idêntico ao real.

<h2>🏗️ Estrutura de Pastas (Atualizada)</h2>

A estrutura do projeto foi atualizada com os novos pacotes de observabilidade:


```text
📦src
 ┣ 📂main
 ┃ ┣ 📂java
 ┃ ┃ ┗ 📂com
 ┃ ┃ ┃ ┗ 📂deliverytech
 ┃ ┃ ┃ ┃ ┗ 📂delivery
 ┃ ┃ ┃ ┃ ┃ ┣ 📂config
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜MicrometerConfig.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ModelMapperConfig.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜RedisConfig.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜SecurityConfig.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜SwaggerConfig.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📂controller
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂auth
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜AuthController.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜UsuarioController.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ClienteController.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜DashboardController.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜EnderecoController.java
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
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂request
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜AtribuirEntregadorDTO.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ClienteDTO.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜EnderecoDTO.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜GrupoOpcionalDTO.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ItemOpcionalDTO.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ItemPedidoDTO.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜PedidoDTO.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ProdutoDTO.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜RestauranteDTO.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜StatusPedidoDTO.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📂response
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ApiResponseWrapper.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜CalculoPedidoDTO.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜CalculoPedidoResponseDTO.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ClienteResponseDTO.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ContagemDTO.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜EnderecoResponseDTO.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ErrorResponse.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ItemPedidoResponseDTO.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜PagedResponseWrapper.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜PedidoResponseDTO.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ProdutoResponseDTO.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜RestauranteResponseDTO.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📂entity
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜Cliente.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜Endereco.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜GrupoOpcional.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ItemOpcional.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ItemPedido.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ItemPedidoOpcional.java
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
 ┃ ┃ ┃ ┃ ┃ ┣ 📂filter
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜CorrelationIdFilter.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📂health
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜DatabaseHealthIndicator.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜ExternalServiceHealthIndicator.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📂repository
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂auth
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜UsuarioRepository.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ClienteRepository.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜EnderecoRepository.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜GrupoOpcionalRepository.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ItemOpcionalRepository.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ItemPedidoOpcionalRepository.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜PedidoRepository.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ProdutoRepository.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜RestauranteRepository.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📂security
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📂jwt
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜JwtAuthenticationFilter.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜JwtUtil.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜SecurityUtils.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📂service
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂alert
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜AlertService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂audit
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜AuditService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂auth
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜AuthService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜UsuarioService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂impl
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ClienteServiceImpl.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜EnderecoServiceImpl.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜PaymentServiceImpl.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜PedidoServiceImpl.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ProdutoServiceImpl.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜RelatorioServiceImpl.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜RestauranteServiceImpl.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜UsuarioServiceImpl.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂metrics
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜MetricsService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ClienteService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜EnderecoService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜PaymentService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜PedidoService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ProdutoService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜RelatorioService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜RestauranteService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜TracingService.java
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
 ┃ ┃ ┃ ┃ ┃ ┗ 📜DeliveryApiApplication.java
 ┃ ┗ 📂resources
 ┃ ┃ ┣ 📂templates
 ┃ ┃ ┃ ┗ 📜dashboard.html
 ┃ ┃ ┣ 📜application-docker.properties
 ┃ ┃ ┣ 📜application.properties
 ┃ ┃ ┣ 📜application.yml
 ┃ ┃ ┣ 📜data-h2.sql
 ┃ ┃ ┣ 📜data-mysql.sql
 ┃ ┃ ┗ 📜logback-spring.xml
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
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜PedidoControllerIntegration.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📂entity
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ClienteTest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ProdutoTest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜RestauranteTest.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📂integration
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜EnderecoControllerIntegrationTest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ProdutoServiceCacheTest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜SwaggerIntegrationTest.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📂security
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📂jwt
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜JwtAuthenticationFilterTest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜JwtUtilTest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜SecurityUtilsTest.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📂service
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ClienteServiceTest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜EnderecoServiceImplTest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜PedidoServiceTest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ProdutoServiceTest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜RestauranteServiceTest.java
 ┃ ┃ ┃ ┃ ┃ ┗ 📂validation
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜CpfValidatorTest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜HorarioFuncionamentoValidatorTest.java
 ┃ ┗ 📂resources
 ┃ ┃ ┗ 📜application-test.properties
```
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

(Novo) MetricsService: Centraliza a criação e incremento de métricas de negócio (pedidos, receita).

(Novo) AuditService: Centraliza o registro de logs de auditoria (quem fez o quê).

(Novo) AlertService: Monitora métricas e saúde em tempo real para disparar alertas.

<h2>📦 DTOs e Validações</h2>

Auth DTOs: LoginRequest, LoginResponse (com token), RegisterRequest, UserResponse (DTO seguro, sem senha).

Request DTOs: ClienteDTO, RestauranteDTO, ProdutoDTO, PedidoDTO, ItemPedidoDTO.

Response DTOs: ClienteResponseDTO, RestauranteResponseDTO, ProdutoResponseDTO, PedidoResponseDTO, e wrappers de resposta (ApiResponseWrapper, PagedResponseWrapper).

Validações: @Valid, @NotNull, @NotBlank, @Email, @Size, e validações customizadas.

<h2>📋 Endpoints REST (Principais)</h2>

A API é dividida em endpoints públicos (para consulta) e protegidos (que exigem autenticação e autorização). Para uma lista completa e interativa, acesse o Swagger UI.

Base URL: http://localhost:8080/api

<h3>🔑 Autenticação (Público)</h3>

POST /auth/login: Autentica um usuário e retorna um token JWT.

POST /auth/register: Registra um novo usuário (CLIENTE ou RESTAURANTE).

<h3>🍽️ Endpoints Públicos (Consulta)</h3>

GET /restaurantes: Lista restaurantes (com filtros).

GET /restaurantes/{id}: Busca um restaurante por ID.

GET /restaurantes/{id}/produtos: Lista o cardápio (produtos) de um restaurante.

GET /produtos/{id}: Busca um produto por ID.

POST /pedidos/calcular: Calcula o total de um pedido (sem salvar).

GET /actuator/health: (Novo) Endpoint de saúde da aplicação.

GET /dashboard: (Novo) Página web do dashboard de monitoramento.

GET /dashboard/api/metrics: (Novo) API de métricas para o dashboard.

<h3>🛡️ Endpoints Protegidos (Requerem Token)</h3>

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

GET /actuator/info (e outros): (Novo) Endpoints sensíveis do Actuator (ADMIN).

<h2>🌟 Padronização de Respostas</h2>

(Sua seção original foi mantida intacta)

Sucesso (2xx) e Paginação
Respostas de sucesso seguem um wrapper padrão (ApiResponseWrapper) e as respostas paginadas (PagedResponseWrapper) incluem metadados de paginação.

Erros (4xx / 5xx)
Erros de validação, autenticação e autorização seguem um padrão (ErrorResponse).

Erro 401 (Unauthorized) - (Token ausente, inválido ou expirado)

Erro 403 (Forbidden) - (Usuário não tem permissão)

Erro 400 (Bad Request) - (Validação de DTO)

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

(Novo) MetricsService: Centraliza a criação e incremento de métricas de negócio (pedidos, receita).

(Novo) AuditService: Centraliza o registro de logs de auditoria (quem fez o quê).

(Novo) AlertService: Monitora métricas e saúde em tempo real para disparar alertas.

<h2>📦 DTOs e Validações</h2>

Auth DTOs: LoginRequest, LoginResponse (com token), RegisterRequest, UserResponse (DTO seguro, sem senha).

Request DTOs: ClienteDTO, RestauranteDTO, ProdutoDTO, PedidoDTO, ItemPedidoDTO.

Response DTOs: ClienteResponseDTO, RestauranteResponseDTO, ProdutoResponseDTO, PedidoResponseDTO, e wrappers de resposta (ApiResponseWrapper, PagedResponseWrapper).

Validações: @Valid, @NotNull, @NotBlank, @Email, @Size, e validações customizadas.

<h2>📋 Endpoints REST (Principais)</h2>

A API é dividida em endpoints públicos (para consulta) e protegidos (que exigem autenticação e autorização). Para uma lista completa e interativa, acesse o Swagger UI.

Base URL: http://localhost:8080/api

<h3>🔑 Autenticação (Público)</h3>

POST /auth/login: Autentica um usuário e retorna um token JWT.

POST /auth/register: Registra um novo usuário (CLIENTE ou RESTAURANTE).

<h3>🍽️ Endpoints Públicos (Consulta)</h3>

GET /restaurantes: Lista restaurantes (com filtros).

GET /restaurantes/{id}: Busca um restaurante por ID.

GET /restaurantes/{id}/produtos: Lista o cardápio (produtos) de um restaurante.

GET /produtos/{id}: Busca um produto por ID.

POST /pedidos/calcular: Calcula o total de um pedido (sem salvar).

GET /actuator/health: (Novo) Endpoint de saúde da aplicação.

GET /dashboard: (Novo) Página web do dashboard de monitoramento.

GET /dashboard/api/metrics: (Novo) API de métricas para o dashboard.

<h3>🛡️ Endpoints Protegidos (Requerem Token)</h3>

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

GET /actuator/info (e outros): (Novo) Endpoints sensíveis do Actuator (ADMIN).

<h2>🌟 Padronização de Respostas</h2>

(Sua seção original foi mantida intacta)

Sucesso (2xx) e Paginação
Respostas de sucesso seguem um wrapper padrão (ApiResponseWrapper) e as respostas paginadas (PagedResponseWrapper) incluem metadados de paginação.

Erros (4xx / 5xx)
Erros de validação, autenticação e autorização seguem um padrão (ErrorResponse).

Erro 401 (Unauthorized) - (Token ausente, inválido ou expirado)

Erro 403 (Forbidden) - (Usuário não tem permissão)

Erro 400 (Bad Request) - (Validação de DTO)
<h2>🧪 Testes Automatizados</h2>

Este projeto possui uma suíte robusta de testes automatizados (agora com 108+ testes) para garantir a qualidade e estabilidade do código, cobrindo:

Testes Unitários (Services): Verificam as regras de negócio de forma isolada (ClienteServiceTest, PedidoServiceTest, TracingServiceTest).

Testes de Integração (Controllers): Verificam a API de ponta a ponta, simulando requisições HTTP (ClienteControllerIntegrationTest, etc.).

Testes de Documentação: Verificam se a documentação Swagger está sendo gerada corretamente (SwaggerIntegrationTest).

Adaptações da Atividade: Os testes PedidoServiceTest e PedidoControllerIntegrationTest foram corrigidos para incluir os mocks e configurações de setup (como @DirtiesContext e MockedStatic) necessários após a injeção dos novos serviços (MetricsService, AuditService), garantindo que o BUILD SUCCESS fosse mantido.

Como Executar os Testes
Bash

# Executa toda a suíte (unitários + integração) e gera o relatório

./mvnw clean install

<h2>📊 Relatório de Cobertura de Código (JaCoCo)</h2>

O projeto está configurado com o JaCoCo para monitorar a cobertura dos testes.

Gere o relatório:

Bash

./mvnw clean install

Abra o relatório no seu navegador: O relatório estará em target/site/jacoco/index.html.

👨‍💻 Desenvolvedor Dimas Aparecido Rabelo

🎓 Curso: Arquitetura de Sistemas 💻 Tecnologias: Java 21 | Spring Boot | Spring Security | JWT | H2 | Maven | Swagger | Actuator | Micrometer | Prometheus | Tracing | Logback 📍 Projeto desenvolvido para módulos de API REST, Serviços, Segurança e Observabilidade.
