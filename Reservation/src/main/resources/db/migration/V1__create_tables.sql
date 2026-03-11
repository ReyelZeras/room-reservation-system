CREATE TABLE usuario (
    usuario_id UUID PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    cargo VARCHAR(100)
);

CREATE TABLE sala (
    sala_id UUID PRIMARY KEY,
    nome VARCHAR(255) NOT NULL UNIQUE,
    capacidade INT NOT NULL,
    status VARCHAR(50) NOT NULL,
    localizacao VARCHAR(255)
);

CREATE TABLE reserva (
    reserva_id UUID PRIMARY KEY,
    sala_id UUID NOT NULL,
    usuario_id UUID NOT NULL,
    data_hora_inicio TIMESTAMP NOT NULL,
    data_hora_fim TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    CONSTRAINT fk_sala FOREIGN KEY (sala_id) REFERENCES sala(sala_id),
    CONSTRAINT fk_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(usuario_id)
);