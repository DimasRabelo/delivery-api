-- Inserir usuários de teste (senha: 123456)
INSERT INTO usuario (id, nome, email, senha, role, ativo, data_criacao, restaurante_id) VALUES
(1, 'Admin Sistema', 'admin@delivery.com', '$2a$10$JuBbqZiLCpP6KaL2XsRmwenlaj47VC/Bp/lldCjvPKb/kVSq45mMC', 'ADMIN', true, NOW(), null),
(2, 'João Cliente', 'joao@email.com', '$2a$10$JuBbqZiLCpP6KaL2XsRmwenlaj47VC/Bp/lldCjvPKb/kVSq45mMC', 'CLIENTE', true, NOW(), null),
(3, 'Maria Cliente', 'maria@email.com', '$2a$10$JuBbqZiLCpP6KaL2XsRmwenlaj47VC/Bp/lldCjvPKb/kVSq45mMC', 'CLIENTE', true, NOW(), null),
(4, 'Pizza Palace', 'pizza@palace.com', '$2a$10$JuBbqZiLCpP6KaL2XsRmwenlaj47VC/Bp/lldCjvPKb/kVSq45mMC', 'RESTAURANTE', true, NOW(), 1),
(5, 'Burger King', 'burger@king.com', '$2a$10$JuBbqZiLCpP6KaL2XsRmwenlaj47VC/Bp/lldCjvPKb/kVSq45mMC', 'RESTAURANTE', true, NOW(), 2),
(6, 'Carlos Entregador', 'carlos@entrega.com', '$2a$10$JuBbqZiLCpP6KaL2XsRmwenlaj47VC/Bp/lldCjvPKb/kVSq45mMC', 'ENTREGADOR', true, NOW(), null);


--Senha para todos os usuários: "123456"


-- Inserir restaurantes
INSERT INTO restaurante (id, nome, endereco, telefone, categoria) VALUES
(1, 'Pizza Palace', 'Rua das Pizzas, 123', '(11) 1234-5678', 'Italiana'),
(2, 'Burger King', 'Av. dos Hambúrgueres, 456', '(11) 8765-4321', 'Fast Food');


-- Inserir produtos
INSERT INTO produto (id, nome, descricao, preco, categoria, disponivel, restaurante_id) VALUES
(1, 'Pizza Margherita', 'Pizza com molho de tomate, mussarela e manjericão', 35.90, 'Pizza', true, 1),
(2, 'Pizza Pepperoni', 'Pizza com molho de tomate, mussarela e pepperoni', 42.90, 'Pizza', true, 1),
(3, 'Whopper', 'Hambúrguer com carne grelhada, alface, tomate, cebola', 28.90, 'Hambúrguer', true, 2),
(4, 'Big King', 'Dois hambúrgueres, alface, queijo, molho especial', 32.90, 'Hambúrguer', true, 2);