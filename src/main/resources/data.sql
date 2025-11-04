-- ==============================
-- RESTAURANTES (DEVE VIR PRIMEIRO)
-- ==============================
INSERT INTO restaurante (id, nome, endereco, telefone, categoria, ativo, taxa_entrega) VALUES
(1, 'Pizza Palace', 'Rua das Pizzas, 123', '(11) 1234-5678', 'Italiana', true, 5.00),
(2, 'Burger King', 'Av. dos Hambúrgueres, 456', '(11) 8765-4321', 'Fast Food', true, 3.50);


-- ==============================
-- USUÁRIOS (Agora pode referenciar os restaurantes 1 e 2)
-- Senha para todos: "123456"
-- ==============================
INSERT INTO usuario (id, nome, email, senha, role, ativo, data_criacao, restaurante_id) VALUES
(1, 'Admin Sistema', 'admin@delivery.com', '$2a$10$AkKArXqwoK8Ocri.T5C8B.4qi4FmFwDWI2aV2zTXFH3CQYwzOQULa', 'ADMIN', true, NOW(), null),
(2, 'João Cliente', 'joao@email.com', '$2a$10$AkKArXqwoK8Ocri.T5C8B.4qi4FmFwDWI2aV2zTXFH3CQYwzOQULa', 'CLIENTE', true, NOW(), null),
(3, 'Maria Cliente', 'maria@email.com', '$2a$10$AkKArXqwoK8Ocri.T5C8B.4qi4FmFwDWI2aV2zTXFH3CQYwzOQULa', 'CLIENTE', true, NOW(), null),
(4, 'Pizza Palace', 'pizza@palace.com', '$2a$10$AkKArXqwoK8Ocri.T5C8B.4qi4FmFwDWI2aV2zTXFH3CQYwzOQULa', 'RESTAURANTE', true, NOW(), 1),
(5, 'Burger King', 'burger@king.com', '$2a$10$AkKArXqwoK8Ocri.T5C8B.4qi4FmFwDWI2aV2zTXFH3CQYwzOQULa', 'RESTAURANTE', true, NOW(), 2),
(6, 'Carlos Entregador', 'carlos@entrega.com', '$2a$10$AkKArXqwoK8Ocri.T5C8B.4qi4FmFwDWI2aV2zTXFH3CQYwzOQULa', 'ENTREGADOR', true, NOW(), null);


-- ==============================
-- CLIENTES (Agora pode referenciar os usuários 2 e 3)
-- ==============================
INSERT INTO cliente (id, nome, email, cpf, ativo, data_cadastro, endereco) VALUES
(2, 'João Cliente', 'joao@email.com', '11122233344', true, NOW(), 'Rua Fictícia do João, 10'),
(3, 'Maria Cliente', 'maria@email.com', '55566677788', true, NOW(), 'Avenida da Maria, 20');


-- ==============================
-- PRODUTOS (Agora pode referenciar os restaurantes 1 e 2)
-- ==============================
INSERT INTO produto (id, nome, descricao, preco, categoria, disponivel, restaurante_id, estoque) VALUES
(1, 'Pizza Margherita', 'Pizza com molho de tomate, mussarela e manjericão', 35.90, 'Pizza', true, 1, 50),
(2, 'Pizza Pepperoni', 'Pizza com molho de tomate, mussarela e pepperoni', 42.90, 'Pizza', true, 1, 50),
(3, 'Whopper', 'Hambúrguer com carne grelhada, alface, tomate, cebola', 28.90, 'Hambúrguer', true, 2, 100),
(4, 'Big King', 'Dois hambúrgueres, alface, queijo, molho especial', 32.90, 'Hambúrguer', true, 2, 100);