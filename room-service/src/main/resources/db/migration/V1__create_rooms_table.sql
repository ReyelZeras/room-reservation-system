CREATE TABLE rooms (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    capacity INTEGER NOT NULL,
    location VARCHAR(255),
    status VARCHAR(50) NOT NULL
);