-- ==============================
-- CLIENTES
-- ==============================
INSERT INTO cliente (nome, email, telefone, endereco, data_cadastro, ativo) VALUES
('João Silva', 'joao@email.com', '(11) 99999-1111', 'Rua A, 123 - São Paulo/SP', NOW(), true),
('Maria Santos', 'maria@email.com', '(11) 99999-2222', 'Rua B, 456 - São Paulo/SP', NOW(), true),
('Pedro Oliveira', 'pedro@email.com', '(11) 99999-3333', 'Rua C, 789 - São Paulo/SP', NOW(), true),
('Dimas Rabelo', 'dimas@email.com', '(11) 99999-4444', 'Rua D, 101 - São Paulo/SP', NOW(), true),
('Ana Lima', 'ana@email.com', '(11) 99999-5555', 'Rua E, 202 - São Paulo/SP', NOW(), true),
('Rafael Costa', 'rafael@email.com', '(11) 99999-6666', 'Rua F, 303 - São Paulo/SP', NOW(), true),
('Carlos Mendes', 'carlos@email.com', '(11) 99999-7777', 'Rua G, 404 - São Paulo/SP', NOW(), true),
('Fernanda Rocha', 'fernanda@email.com', '(11) 99999-8888', 'Rua H, 505 - São Paulo/SP', NOW(), true),
('Lucas Almeida', 'lucas@email.com', '(11) 99999-9999', 'Rua I, 606 - São Paulo/SP', NOW(), true),
('Juliana Martins', 'juliana@email.com', '(11) 98888-1111', 'Rua J, 707 - São Paulo/SP', NOW(), true),
('Bruno Carvalho', 'bruno@email.com', '(11) 98888-2222', 'Rua K, 808 - São Paulo/SP', NOW(), true),
('Marcos Silva', 'marcos@email.com', '(11) 98888-3333', 'Rua L, 909 - São Paulo/SP', NOW(), true),
('Patrícia Lima', 'patricia@email.com', '(11) 98888-4444', 'Rua M, 1010 - São Paulo/SP', NOW(), true),
('Ricardo Souza', 'ricardo@email.com', '(11) 98888-5555', 'Rua N, 1111 - São Paulo/SP', NOW(), true),
('Camila Ferreira', 'camila@email.com', '(11) 98888-6666', 'Rua O, 1212 - São Paulo/SP', NOW(), true),
('Diego Torres', 'diego@email.com', '(11) 98888-7777', 'Rua P, 1313 - São Paulo/SP', NOW(), true);



-- ==============================
-- RESTAURANTES
-- ==============================
INSERT INTO restaurante (nome, categoria, endereco, telefone, taxa_entrega, avaliacao, ativo) VALUES
('Pizzaria Bella', 'Italiana', 'Av. Paulista, 1000 - São Paulo/SP', '(11) 3333-1111', 5.00, 4.5, true),
('Burger House', 'Hamburgueria', 'Rua Augusta, 500 - São Paulo/SP', '(11) 3333-2222', 3.50, 4.2, true),
('Sushi Master', 'Japonesa', 'Rua Liberdade, 200 - São Paulo/SP', '(11) 3333-3333', 8.00, 4.8, true),
('Veggie Fresh', 'Saudável', 'Rua das Flores, 150 - São Paulo/SP', '(11) 3333-4444', 4.00, 4.6, true),
('Taco Town', 'Mexicana', 'Av. Brasil, 1200 - São Paulo/SP', '(11) 4444-1111', 5.00, 4.4, true),
('Café Central', 'Cafeteria', 'Rua das Acácias, 300 - São Paulo/SP', '(11) 4444-2222', 3.00, 4.7, true),
('Bistrô Paris', 'Francesa', 'Rua da Paz, 400 - São Paulo/SP', '(11) 4444-3333', 6.00, 4.9, true),
('Churrascaria Grill', 'Churrasco', 'Av. Ipiranga, 1500 - São Paulo/SP', '(11) 4444-4444', 7.00, 4.5, true),
('Padaria Pão Nosso', 'Padaria', 'Rua das Oliveiras, 200 - São Paulo/SP', '(11) 4444-5555', 2.50, 4.8, true);
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

-- Taco Town
INSERT INTO produto (nome, descricao, preco, categoria, disponivel, restaurante_id) VALUES
('Taco de Frango', 'Taco com frango grelhado e molho especial', 19.90, 'Taco', true, 5),
('Taco Vegano', 'Taco com legumes e guacamole', 18.90, 'Taco', true, 5),
('Nachos Grande', 'Porção de nachos com queijo', 25.90, 'Acompanhamento', true, 5),
('Burrito', 'Burrito de carne com arroz e feijão', 28.90, 'Burrito', true, 5),
('Quesadilla', 'Quesadilla de queijo e frango', 22.90, 'Quesadilla', true, 5);

-- Café Central
INSERT INTO produto (nome, descricao, preco, categoria, disponivel, restaurante_id) VALUES
('Cappuccino', 'Café com leite e espuma', 9.90, 'Bebida', true, 6),
('Expresso Duplo', 'Café expresso duplo', 7.90, 'Bebida', true, 6),
('Bolo de Cenoura', 'Bolo caseiro com cobertura de chocolate', 12.90, 'Sobremesa', true, 6),
('Sanduíche Natural', 'Pão integral, frango e vegetais', 18.90, 'Lanche', true, 6),
('Suco Laranja', 'Suco natural de laranja', 8.90, 'Bebida', true, 6);

-- Bistrô Paris
INSERT INTO produto (nome, descricao, preco, categoria, disponivel, restaurante_id) VALUES
('Crepe de Nutella', 'Crepe doce com Nutella', 15.90, 'Sobremesa', true, 7),
('Quiche de Espinafre', 'Quiche de espinafre com queijo', 18.90, 'Lanche', true, 7),
('Croissant', 'Croissant amanteigado', 12.90, 'Lanche', true, 7),
('Salada Niçoise', 'Salada francesa com atum e ovos', 25.90, 'Salada', true, 7),
('Sopa de Legumes', 'Sopa leve de legumes variados', 19.90, 'Sopa', true, 7);

-- Churrascaria Grill
INSERT INTO produto (nome, descricao, preco, categoria, disponivel, restaurante_id) VALUES
('Picanha Grelhada', 'Picanha fatiada com farofa', 55.90, 'Carne', true, 8),
('Fraldinha', 'Fraldinha grelhada', 49.90, 'Carne', true, 8),
('Linguiça Artesanal', 'Linguiça suína artesanal', 29.90, 'Carne', true, 8),
('Batata Rústica', 'Batata com alecrim e azeite', 18.90, 'Acompanhamento', true, 8),
('Salada Mista', 'Folhas verdes e legumes', 15.90, 'Salada', true, 8);

-- Padaria Pão Nosso
INSERT INTO produto (nome, descricao, preco, categoria, disponivel, restaurante_id) VALUES
('Pão Francês', 'Pão fresco da manhã', 0.90, 'Pão', true, 9),
('Croissant Doce', 'Croissant com chocolate', 3.50, 'Doce', true, 9),
('Bolo de Chocolate', 'Bolo de chocolate caseiro', 18.90, 'Sobremesa', true, 9),
('Café Expresso', 'Café expresso simples', 6.90, 'Bebida', true, 9),
('Suco Natural', 'Suco de frutas da estação', 7.90, 'Bebida', true, 9);





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
('PED1234567897', NOW(), 'CANCELADO', 22.90, 22.90, 3.50, 'Cliente desistiu do pedido', 2, 2, 'Rua B, 456 - São Paulo/SP'),
('PED1234567898', '2022-03-15 19:30:00', 'ENTREGUE', 44.80, 40.80, 4.00, '', 7, 5, 'Rua G, 404 - São Paulo/SP'),
('PED1234567899', '2022-05-22 12:15:00', 'ENTREGUE', 32.80, 29.80, 3.00, 'Sem queijo extra', 8, 6, 'Rua H, 505 - São Paulo/SP'),
('PED1234567900', '2022-08-10 18:50:00', 'ENTREGUE', 58.80, 54.80, 4.00, '', 9, 7, 'Rua I, 606 - São Paulo/SP'),
('PED1234567901', '2022-11-03 20:05:00', 'ENTREGUE', 36.80, 32.80, 4.00, '', 10, 5, 'Rua J, 707 - São Paulo/SP'),
('PED1234567902', '2023-01-15 13:45:00', 'ENTREGUE', 48.80, 44.80, 4.00, '', 11, 6, 'Rua K, 808 - São Paulo/SP'),
('PED1234567903', '2023-03-10 19:00:00', 'CANCELADO', 22.90, 22.90, 3.50, 'Cliente desistiu', 7, 5, 'Rua G, 404 - São Paulo/SP'),
('PED1234567904', '2023-05-28 14:20:00', 'ENTREGUE', 58.80, 54.80, 4.00, '', 8, 6, 'Rua H, 505 - São Paulo/SP'),
('PED1234567905', '2023-07-17 18:35:00', 'ENTREGUE', 41.80, 38.80, 3.00, '', 9, 7, 'Rua I, 606 - São Paulo/SP'),
('PED1234567906', '2023-09-05 20:10:00', 'ENTREGUE', 72.80, 68.80, 4.00, 'Sem cebola', 10, 5, 'Rua J, 707 - São Paulo/SP'),
('PED1234567907', '2023-10-01 19:50:00', 'ENTREGUE', 36.80, 32.80, 4.00, '', 11, 6, 'Rua K, 808 - São Paulo/SP'),
('PED1234567908', '2023-12-11 12:25:00', 'CANCELADO', 48.80, 44.80, 4.00, '', 7, 7, 'Rua G, 404 - São Paulo/SP'),
('PED1234567909', '2024-02-08 13:30:00', 'ENTREGUE', 22.90, 22.90, 3.50, '', 8, 5, 'Rua H, 505 - São Paulo/SP'),
('PED1234567910', '2024-04-15 21:10:00', 'ENTREGUE', 58.80, 54.80, 4.00, '', 9, 6, 'Rua I, 606 - São Paulo/SP'),
('PED1234567911', '2024-06-25 19:05:00', 'ENTREGUE', 41.80, 38.80, 3.00, '', 10, 7, 'Rua J, 707 - São Paulo/SP'),
('PED1234567912', '2024-09-18 18:45:00', 'ENTREGUE', 72.80, 68.80, 4.00, '', 11, 5, 'Rua K, 808 - São Paulo/SP'),
('PED1234567913', '2025-01-12 20:30:00', 'ENTREGUE', 36.80, 32.80, 4.00, '', 7, 6, 'Rua G, 404 - São Paulo/SP'),
('PED1234567914', '2025-03-21 19:15:00', 'CANCELADO', 48.80, 44.80, 4.00, 'Cliente desistiu', 8, 7, 'Rua H, 505 - São Paulo/SP'),
('PED1234567915', '2025-05-30 14:50:00', 'SAIU_PARA_ENTREGA', 22.90, 22.90, 3.50, '', 9, 5, 'Rua I, 606 - São Paulo/SP'),
('PED1234567916', '2025-08-10 18:05:00', 'ENTREGUE', 58.80, 54.80, 4.00, '', 10, 6, 'Rua J, 707 - São Paulo/SP'),
('PED1234567917', '2025-10-05 20:20:00', 'PREPARANDO', 41.80, 38.80, 3.00, '', 11, 7, 'Rua K, 808 - São Paulo/SP');


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

-- Pedido 7898
INSERT INTO itens_pedido (quantidade, preco_unitario, subtotal, pedido_id, produto_id) VALUES
(1, 19.90, 19.90, 9, 13),
(1, 24.90, 24.90, 9, 17);

-- Pedido 7899
INSERT INTO itens_pedido (quantidade, preco_unitario, subtotal, pedido_id, produto_id) VALUES
(1, 12.90, 12.90, 10, 15),
(1, 16.90, 16.90, 10, 18);

-- Pedido 7900
INSERT INTO itens_pedido (quantidade, preco_unitario, subtotal, pedido_id, produto_id) VALUES
(2, 12.90, 25.80, 11, 17),
(1, 18.90, 18.90, 11, 19);

-- Pedido 7901
INSERT INTO itens_pedido (quantidade, preco_unitario, subtotal, pedido_id, produto_id) VALUES
(1, 19.90, 19.90, 12, 13),
(1, 25.90, 25.90, 12, 16);

-- Pedido 7902
INSERT INTO itens_pedido (quantidade, preco_unitario, subtotal, pedido_id, produto_id) VALUES
(1, 12.90, 12.90, 13, 15),
(1, 18.90, 18.90, 13, 18);

-- Pedido 7903
INSERT INTO itens_pedido (quantidade, preco_unitario, subtotal, pedido_id, produto_id) VALUES
(1, 19.90, 19.90, 14, 13);

-- Pedido 7904
INSERT INTO itens_pedido (quantidade, preco_unitario, subtotal, pedido_id, produto_id) VALUES
(1, 12.90, 12.90, 15, 15),
(2, 7.90, 15.80, 15, 22);

-- Pedido 7905
INSERT INTO itens_pedido (quantidade, preco_unitario, subtotal, pedido_id, produto_id) VALUES
(1, 18.90, 18.90, 16, 19),
(1, 12.90, 12.90, 16, 20);

-- Pedido 7906
INSERT INTO itens_pedido (quantidade, preco_unitario, subtotal, pedido_id, produto_id) VALUES
(1, 19.90, 19.90, 17, 13),
(1, 25.90, 25.90, 17, 16);

-- Pedido 7907
INSERT INTO itens_pedido (quantidade, preco_unitario, subtotal, pedido_id, produto_id) VALUES
(1, 12.90, 12.90, 18, 15);

-- Pedido 7908
INSERT INTO itens_pedido (quantidade, preco_unitario, subtotal, pedido_id, produto_id) VALUES
(1, 19.90, 19.90, 19, 13),
(1, 24.90, 24.90, 19, 17);

-- Pedido 7909
INSERT INTO itens_pedido (quantidade, preco_unitario, subtotal, pedido_id, produto_id) VALUES
(1, 12.90, 12.90, 20, 15);

-- Pedido 7910
INSERT INTO itens_pedido (quantidade, preco_unitario, subtotal, pedido_id, produto_id) VALUES
(1, 18.90, 18.90, 21, 19);

-- Pedido 7911
INSERT INTO itens_pedido (quantidade, preco_unitario, subtotal, pedido_id, produto_id) VALUES
(2, 12.90, 25.80, 22, 15),
(1, 18.90, 18.90, 22, 18);

-- Pedido 7912
INSERT INTO itens_pedido (quantidade, preco_unitario, subtotal, pedido_id, produto_id) VALUES
(1, 19.90, 19.90, 23, 13);

-- Pedido 7913
INSERT INTO itens_pedido (quantidade, preco_unitario, subtotal, pedido_id, produto_id) VALUES
(1, 25.90, 25.90, 24, 16);

-- Pedido 7914
INSERT INTO itens_pedido (quantidade, preco_unitario, subtotal, pedido_id, produto_id) VALUES
(1, 12.90, 12.90, 25, 15);

-- Pedido 7915
INSERT INTO itens_pedido (quantidade, preco_unitario, subtotal, pedido_id, produto_id) VALUES
(1, 19.90, 19.90, 26, 13);

-- Pedido 7916
INSERT INTO itens_pedido (quantidade, preco_unitario, subtotal, pedido_id, produto_id) VALUES
(1, 18.90, 18.90, 27, 19);

-- Pedido 7917
INSERT INTO itens_pedido (quantidade, preco_unitario, subtotal, pedido_id, produto_id) VALUES
(2, 12.90, 25.80, 28, 15);
