-- Insercao de 10 salas padrao para povoar o catalogo e o motor de sugestoes.
-- Utiliza a funcao gen_random_uuid() nativa do PostgreSQL para gerar as chaves primarias.

INSERT INTO rooms (id, name, capacity, location, status) VALUES
(gen_random_uuid(), 'Auditorio Master', 150, 'Andar Principal', 'AVAILABLE'),
(gen_random_uuid(), 'Sala de Reunioes Alpha', 12, 'Andar 1 - Bloco A', 'AVAILABLE'),
(gen_random_uuid(), 'Sala de Reunioes Beta', 10, 'Andar 1 - Bloco B', 'AVAILABLE'),
(gen_random_uuid(), 'Laboratorio de Inovacao', 25, 'Andar 2 - Bloco A', 'AVAILABLE'),
(gen_random_uuid(), 'Sala de Treinamento 1', 30, 'Andar 2 - Bloco C', 'AVAILABLE'),
(gen_random_uuid(), 'Espaco Coworking', 50, 'Terreo', 'AVAILABLE'),
(gen_random_uuid(), 'Sala Executiva Ouro', 8, 'Cobertura', 'AVAILABLE'),
(gen_random_uuid(), 'Sala Executiva Prata', 8, 'Cobertura', 'AVAILABLE'),
(gen_random_uuid(), 'Sala de Foco 1', 4, 'Andar 1 - Bloco C', 'AVAILABLE'),
(gen_random_uuid(), 'Sala de Foco 2', 4, 'Andar 1 - Bloco C', 'AVAILABLE');