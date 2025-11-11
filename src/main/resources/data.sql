/*
 * =================================================================
 * SCRIPT DE CARGA INICIAL (PÓS-REFATORAÇÃO)
 * (Versão corrigida para H2, com nomes de tabela em minúsculo)
 * =================================================================
 */

-- ==============================
-- 1. USUÁRIOS
-- ==============================
INSERT INTO usuario (id, email, senha, role, ativo, data_criacao, restaurante_id) VALUES
(1, 'admin@delivery.com', '$2a$10$AkKArXqwoK8Ocri.T5C8B.4qi4FmFwDWI2aV2zTXFH3CQYwzOQULa', 'ADMIN', true, NOW(), null),
(2, 'joao@email.com', '$2a$10$AkKArXqwoK8Ocri.T5C8B.4qi4FmFwDWI2aV2zTXFH3CQYwzOQULa', 'CLIENTE', true, NOW(), null),
(3, 'maria@email.com', '$2a$10$AkKArXqwoK8Ocri.T5C8B.4qi4FmFwDWI2aV2zTXFH3CQYwzOQULa', 'CLIENTE', true, NOW(), null),
(4, 'pizza@palace.com', '$2a$10$AkKArXqwoK8Ocri.T5C8B.4qi4FmFwDWI2aV2zTXFH3CQYwzOQULa', 'RESTAURANTE', true, NOW(), null),
(5, 'burger@king.com', '$2a$10$AkKArXqwoK8Ocri.T5C8B.4qi4FmFwDWI2aV2zTXFH3CQYwzOQULa', 'RESTAURANTE', true, NOW(), null),
(6, 'carlos@entrega.com', '$2a$10$AkKArXqwoK8Ocri.T5C8B.4qi4FmFwDWI2aV2zTXFH3CQYwzOQULa', 'ENTREGADOR', true, NOW(), null);

-- ==============================
-- 2. ENDEREÇOS
-- ==============================
INSERT INTO endereco (id, usuario_id, rua, numero, bairro, cidade, estado, cep, apelido, ativo) 
VALUES 
(1, 4, 'Rua das Pizzas', '123', 'Centro', 'CidadePizza', 'SP', '01001000', 'Restaurante Pizza Palace', true), 
(2, 5, 'Av. dos Hambúrgueres', '456', 'BairroLanche', 'CidadeBurger', 'SP', '04538133', 'Restaurante Burger King', true), 
(3, 2, 'Rua Fictícia do João', '10', 'BairroJoao', 'CidadeJoao', 'RJ', '20031912', 'Casa João', true), 
(4, 3, 'Avenida da Maria', '20', 'BairroMaria', 'CidadeMaria', 'MG', '30112010', 'Casa Maria', true);

-- ==============================
-- 3. RESTAURANTES
-- ==============================
INSERT INTO restaurante (id, nome, endereco_id, telefone, categoria, ativo, taxa_entrega) VALUES
(1, 'Pizza Palace', 1, '(11) 1234-5678', 'Italiana', true, 5.00),
(2, 'Burger King', 2, '(11) 8765-4321', 'Fast Food', true, 3.50);

-- ==============================
-- 4. UPDATE USUÁRIO (Ciclo de Dependência)
-- ==============================
UPDATE usuario SET restaurante_id = 1 WHERE id = 4;
UPDATE usuario SET restaurante_id = 2 WHERE id = 5;

-- ==============================
-- 5. CLIENTES
-- ==============================
INSERT INTO cliente (id, nome, cpf, telefone) VALUES
(2, 'João Cliente', '11122233344', '(21) 91111-1111'),
(3, 'Maria Cliente', '55566677788', '(11) 92222-2222');

-- ==============================
-- 6. PRODUTOS
-- ==============================
INSERT INTO produto (id, nome, descricao, preco_base, categoria, disponivel, restaurante_id, estoque) VALUES
(1, 'Pizza Margherita', 'Pizza com molho de tomate, mussarela e manjericão', 35.90, 'Pizza', true, 1, 50),
(2, 'Pizza Pepperoni', 'Pizza com molho de tomate, mussarela e pepperoni', 42.90, 'Pizza', true, 1, 50),
(3, 'Whopper', 'Hambúrguer com carne grelhada, alface, tomate, cebola', 28.90, 'Hambúrguer', true, 2, 100),
(4, 'Big King', 'Dois hambúrgueres, alface, queijo, molho especial', 32.90, 'Hambúrguer', true, 2, 100);

-- ==============================
-- 7. GRUPO_OPCIONAL
-- ==============================
INSERT INTO grupo_opcional (id, nome, produto_id, min_selecao, max_selecao) VALUES
(1, 'Escolha a Borda', 1, 1, 1),
(2, 'Escolha a Borda', 2, 1, 1),
(3, 'Bebidas', 1, 0, 2),
(4, 'Bebidas', 2, 0, 2),
(5, 'Molho Extra', 3, 0, 3),
(6, 'Adicionais', 3, 0, 2),
(7, 'Combo Bebida', 4, 0, 1),
(8, 'Molho Extra', 4, 0, 3);

-- ==============================
-- 8. ITEM_OPCIONAL
-- ==============================
INSERT INTO item_opcional (id, nome, grupo_opcional_id, preco_adicional) VALUES
(1, 'Borda Tradicional', 1, 0.00),
(2, 'Borda Catupiry', 1, 8.00),
(3, 'Borda Tradicional', 2, 0.00),
(4, 'Borda Catupiry', 2, 8.00),
(5, 'Borda Cheddar', 2, 9.00),
(6, 'Coca-Cola 2L', 3, 12.00),
(7, 'Guaraná 2L', 3, 10.00),
(8, 'Coca-Cola 2L', 4, 12.00),
(9, 'Guaraná 2L', 4, 10.00),
(10, 'Maionese Temperada', 5, 2.50),
(11, 'Ketchup', 5, 1.50),
(12, 'Maionese Temperada', 8, 2.50),
(13, 'Mostarda', 8, 1.50),
(14, 'Bacon Extra', 6, 4.00),
(15, 'Queijo Extra', 6, 3.00),
(16, 'Coca-Cola Lata', 7, 6.00),
(17, 'Suco de Laranja', 7, 7.00);

-- ==========================================================
-- 9. PEDIDOS
-- ==========================================================
INSERT INTO pedido (id, numero_pedido, cliente_id, restaurante_id, endereco_entrega_id, data_pedido, status, metodo_pagamento, subtotal, taxa_entrega, valor_total) VALUES
(1, 'uuid-joao-001', 2, 1, 3, NOW(), 'PENDENTE', 'PIX', 55.90, 5.00, 60.90);

INSERT INTO itens_pedido (id, pedido_id, produto_id, quantidade, preco_unitario, subtotal) VALUES
(1, 1, 1, 1, 55.90, 55.90);

INSERT INTO item_pedido_opcional (id, item_pedido_id, item_opcional_id, preco_registrado) VALUES
(1, 1, 2, 8.00),
(2, 1, 6, 12.00);

INSERT INTO pedido (id, numero_pedido, cliente_id, restaurante_id, endereco_entrega_id, data_pedido, status, metodo_pagamento, entregador_id, subtotal, taxa_entrega, valor_total) VALUES
(2, 'uuid-maria-001', 3, 2, 4, NOW(), 'SAIU_PARA_ENTREGA', 'CARTAO_CREDITO', 6, 40.90, 3.50, 44.40);

INSERT INTO itens_pedido (id, pedido_id, produto_id, quantidade, preco_unitario, subtotal) VALUES
(2, 2, 3, 1, 40.90, 40.90);

INSERT INTO item_pedido_opcional (id, item_pedido_id, item_opcional_id, preco_registrado) VALUES
(3, 2, 14, 4.00);


-- ==============================
-- 10. RESETAR AS SEQUÊNCIAS (SINTAXE CORRIGIDA PARA H2)
-- ==============================
ALTER TABLE usuario ALTER COLUMN ID RESTART WITH 7;
ALTER TABLE endereco ALTER COLUMN ID RESTART WITH 5;
ALTER TABLE restaurante ALTER COLUMN ID RESTART WITH 3;
ALTER TABLE produto ALTER COLUMN ID RESTART WITH 5;
ALTER TABLE grupo_opcional ALTER COLUMN ID RESTART WITH 9;
ALTER TABLE item_opcional ALTER COLUMN ID RESTART WITH 18;
ALTER TABLE pedido ALTER COLUMN ID RESTART WITH 3;
ALTER TABLE itens_pedido ALTER COLUMN ID RESTART WITH 3;
ALTER TABLE item_pedido_opcional ALTER COLUMN ID RESTART WITH 6;