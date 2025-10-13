-- ==============================
-- CLIENTES
-- ==============================
INSERT INTO cliente (nome, email, telefone, endereco, data_cadastro, ativo) VALUES
('João Silva', 'joao@email.com', '(11) 99999-1111', 'Rua A, 123 - São Paulo/SP', NOW(), true),
('Maria Santos', 'maria@email.com', '(11) 99999-2222', 'Rua B, 456 - São Paulo/SP', NOW(), true),
('Pedro Oliveira', 'pedro@email.com', '(11) 99999-3333', 'Rua C, 789 - São Paulo/SP', NOW(), true),
('Dimas Rabelo', 'dimas@email.com', '(11) 99999-4444', 'Rua D, 101 - São Paulo/SP', NOW(), true),
('Ana Lima', 'ana@email.com', '(11) 99999-5555', 'Rua E, 202 - São Paulo/SP', NOW(), true),
('Rafael Costa', 'rafael@email.com', '(11) 99999-6666', 'Rua F, 303 - São Paulo/SP', NOW(), true);

-- ==============================
-- RESTAURANTES
-- ==============================
INSERT INTO restaurante (nome, categoria, endereco, telefone, taxa_entrega, avaliacao, ativo) VALUES
('Pizzaria Bella', 'Italiana', 'Av. Paulista, 1000 - São Paulo/SP', '(11) 3333-1111', 5.00, 4.5, true),
('Burger House', 'Hamburgueria', 'Rua Augusta, 500 - São Paulo/SP', '(11) 3333-2222', 3.50, 4.2, true),
('Sushi Master', 'Japonesa', 'Rua Liberdade, 200 - São Paulo/SP', '(11) 3333-3333', 8.00, 4.8, true),
('Veggie Fresh', 'Saudável', 'Rua das Flores, 150 - São Paulo/SP', '(11) 3333-4444', 4.00, 4.6, true);

-- ==============================
-- PRODUTOS
-- ==============================

-- Pizzaria Bella
INSERT INTO produto (nome, descricao, preco, categoria, disponivel, restaurante_id) VALUES
('Pizza Margherita', 'Molho de tomate, mussarela e manjericão', 35.90, 'Pizza', true, 1),
('Pizza Calabresa', 'Molho de tomate, mussarela e calabresa', 38.90, 'Pizza', true, 1),
('Lasanha Bolonhesa', 'Lasanha tradicional com molho bolonhesa', 28.90, 'Massa', true, 1);

-- Burger House
INSERT INTO produto (nome, descricao, preco, categoria, disponivel, restaurante_id) VALUES
('X-Burger', 'Hambúrguer, queijo, alface e tomate', 18.90, 'Hambúrguer', true, 2),
('X-Bacon', 'Hambúrguer, queijo, bacon, alface e tomate', 22.90, 'Hambúrguer', true, 2),
('Batata Frita', 'Porção de batata frita crocante', 12.90, 'Acompanhamento', true, 2);

-- Sushi Master
INSERT INTO produto (nome, descricao, preco, categoria, disponivel, restaurante_id) VALUES
('Combo Sashimi', '15 peças de sashimi variado', 45.90, 'Sashimi', true, 3),
('Hot Roll Salmão', '8 peças de hot roll de salmão', 32.90, 'Hot Roll', true, 3),
('Temaki Atum', 'Temaki de atum com cream cheese', 15.90, 'Temaki', true, 3);

-- Veggie Fresh
INSERT INTO produto (nome, descricao, preco, categoria, disponivel, restaurante_id) VALUES
('Salada Tropical', 'Folhas, frutas e molho de maracujá', 24.90, 'Salada', true, 4),
('Wrap de Frango', 'Frango grelhado com legumes e molho leve', 21.90, 'Wrap', true, 4),
('Suco Verde', 'Suco detox natural', 9.90, 'Bebida', true, 4);

-- ==============================
-- PEDIDOS
-- ==============================
INSERT INTO pedido 
(numero_pedido, data_pedido, status, valor_total, subtotal, taxa_entrega, observacoes, cliente_id, restaurante_id, endereco_entrega) VALUES
('PED1234567890', NOW(), 'PENDENTE', 64.80, 64.80, 5.00, 'Sem cebola na pizza', 1, 1, 'Rua A, 123 - São Paulo/SP'),
('PED1234567891', NOW(), 'CONFIRMADO', 41.80, 41.80, 3.50, '', 2, 2, 'Rua B, 456 - São Paulo/SP'),
('PED1234567892', NOW(), 'ENTREGUE', 78.80, 78.80, 8.00, 'Wasabi à parte', 3, 3, 'Rua C, 789 - São Paulo/SP'),
('PED1234567893', NOW(), 'PENDENTE', 74.80, 74.80, 5.00, 'Sem molho na pizza', 4, 1, 'Rua D, 101 - São Paulo/SP'),
('PED1234567894', NOW(), 'PREPARANDO', 31.80, 31.80, 3.50, 'Adicionar maionese extra', 5, 2, 'Rua E, 202 - São Paulo/SP'),
('PED1234567895', NOW(), 'SAIU_PARA_ENTREGA', 55.80, 55.80, 8.00, '', 6, 3, 'Rua F, 303 - São Paulo/SP'),
('PED1234567896', NOW(), 'ENTREGUE', 46.80, 46.80, 4.00, 'Sem gelo no suco', 5, 4, 'Rua E, 202 - São Paulo/SP'),
('PED1234567897', NOW(), 'CANCELADO', 22.90, 22.90, 3.50, 'Cliente desistiu do pedido', 2, 2, 'Rua B, 456 - São Paulo/SP');
-- ==============================
-- ITENS DE PEDIDO
-- ==============================

-- Pedido 1 (João - Pizzaria Bella)
INSERT INTO itens_pedido (quantidade, preco_unitario, subtotal, pedido_id, produto_id) VALUES
(1, 35.90, 35.90, 1, 1),
(1, 28.90, 28.90, 1, 3);

-- Pedido 2 (Maria - Burger House)
INSERT INTO itens_pedido (quantidade, preco_unitario, subtotal, pedido_id, produto_id) VALUES
(1, 22.90, 22.90, 2, 5),
(1, 18.90, 18.90, 2, 4);

-- Pedido 3 (Pedro - Sushi Master)
INSERT INTO itens_pedido (quantidade, preco_unitario, subtotal, pedido_id, produto_id) VALUES
(1, 45.90, 45.90, 3, 7),
(1, 32.90, 32.90, 3, 8);

-- Pedido 4 (Dimas - Pizzaria Bella)
INSERT INTO itens_pedido (quantidade, preco_unitario, subtotal, pedido_id, produto_id) VALUES
(1, 35.90, 35.90, 4, 1),
(1, 38.90, 38.90, 4, 2);

-- Pedido 5 (Ana - Burger House)
INSERT INTO itens_pedido (quantidade, preco_unitario, subtotal, pedido_id, produto_id) VALUES
(1, 18.90, 18.90, 5, 4),
(1, 12.90, 12.90, 5, 6);

-- Pedido 6 (Rafael - Sushi Master)
INSERT INTO itens_pedido (quantidade, preco_unitario, subtotal, pedido_id, produto_id) VALUES
(1, 45.90, 45.90, 6, 7),
(1, 9.90, 9.90, 6, 9);

-- Pedido 7 (Ana - Veggie Fresh)
INSERT INTO itens_pedido (quantidade, preco_unitario, subtotal, pedido_id, produto_id) VALUES
(1, 24.90, 24.90, 7, 10),
(1, 21.90, 21.90, 7, 11);

-- Pedido 8 (Maria - Burger House cancelado)
INSERT INTO itens_pedido (quantidade, preco_unitario, subtotal, pedido_id, produto_id) VALUES
(1, 22.90, 22.90, 8, 5);
