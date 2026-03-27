CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255),
    provider VARCHAR(50),
    provider_id VARCHAR(255),
    role VARCHAR(50),
    created_at TIMESTAMP NOT NULL
);

-- Inserção de um usuário administrador inicial para testes
INSERT INTO users (id, username, email, name, provider, role, created_at)
VALUES ('f47ac10b-58cc-4372-a567-0e02b2c3d479', 'admin', 'admin@roomres.com', 'Administrator', 'local', 'ADMIN', NOW());