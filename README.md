# 🍔 DeliveryTech API

Sistema de delivery desenvolvido com **Spring Boot** e **Java 21 LTS** para gerenciar clientes, restaurantes, produtos e pedidos.

---

## 🚀 Tecnologias Utilizadas
- Java 21 LTS  
- Spring Boot 3.5.6  
- Spring Web  
- Spring Data JPA  
- H2 Database (em memória)  
- Maven  

---

## ⚙️ Recursos Modernos (Java 21)
- Records  
- Text Blocks  
- Pattern Matching  
- Virtual Threads  

---

## 🏃‍♂️ Como Executar o Projeto

### 🔹 Pré-requisitos
- JDK 21 instalado  
- Maven configurado (ou usar o wrapper `./mvnw`)

### 🔹 Passos para rodar
1. Clonar o repositório:
   git clone https://github.com/DimasRabelo/delivery-api.git
   cd delivery-api
2. Executar a aplicação:
   ./mvnw spring-boot:run
3. Acessar no navegador:  
   http://localhost:8080

---

## 🧩 Estrutura de Pastas

![Estrutura do Projeto](https://raw.githubusercontent.com/DimasRabelo/delivery-api/main/estrutura.png)

## 🧪 Testes com Postman

Na pasta `postman/` há uma collection chamada **DeliveryAPI.postman_collection.json**, contendo todos os endpoints configurados para teste.

**Como importar:**
1. Abra o Postman  
2. Clique em **Import**  
3. Selecione o arquivo `DeliveryAPI.postman_collection.json`  
4. Execute as requisições para testar os endpoints  

**Essa collection cobre:**
- CRUD completo de Clientes, Restaurantes e Produtos  
- Criação e consulta de Pedidos  
- Testes de filtros, status e valores calculados  

---

## 📋 Principais Endpoints

| Método | Endpoint | Descrição |
|:-------|:----------|:-----------|
| **POST** | `/clientes` | Cadastrar novo cliente |
| **GET** | `/clientes` | Listar todos os clientes |
| **GET** | `/clientes/{id}` | Consultar cliente por ID |
| **PUT** | `/clientes/{id}` | Atualizar dados de cliente |
| **DELETE** | `/clientes/{id}` | Inativar cliente |
| **GET** | `/restaurantes` | Listar restaurantes |
| **GET** | `/restaurantes/categoria/{categoria}` | Buscar por categoria |
| **POST** | `/produtos` | Cadastrar produto |
| **GET** | `/produtos/restaurante/{id}` | Listar produtos de um restaurante |
| **POST** | `/pedidos?clienteId=1&restauranteId=1` | Criar pedido |
| **GET** | `/pedidos/cliente/{id}` | Consultar pedidos por cliente |

---

## 🗄️ Banco de Dados H2

A aplicação utiliza **H2 em memória**.  
Acesse o console em:  
http://localhost:8080/h2-console

**Configurações:**
- JDBC URL: `jdbc:h2:mem:deliverydb`  
- User Name: `sa`  
- Password: *(em branco)*

---

## 📦 Dados Iniciais (data.sql)

O arquivo `data.sql` popula o banco com:  
- Clientes (João, Maria, Pedro, Dimas)  
- Restaurantes (Pizzaria Bella, Burger House, Sushi Master)  
- Produtos e pedidos de exemplo  

---

## 👨‍💻 Desenvolvedor

**Dimas Aparecido Rabelo**  
🎓 Curso: Arquitetura de Sistemas  
💻 Tecnologias: Java 21 | Spring Boot | H2 | Maven  
📍 Projeto desenvolvido para o módulo de **Persistência de Dados**


Banco: H2 em memória

Profile: development

👨‍💻 Desenvolvedor

Dimas Aparecido - Curso Arquitetura de Sistemas  
 Desenvolvido com JDK 21 e Spring Boot 3.2.x
