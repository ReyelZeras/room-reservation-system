-- Define a senha correta encriptada (Hash capturada do Postman) e ativa o admin //Teste132@
UPDATE users
SET password = '$2a$10$ppiV1OUfebpHkJ8AxF6bguB4jMly8bG7W2gde9eI/HJEArc9G.YTi',
    active = true 
WHERE email = 'admin@roomres.com';