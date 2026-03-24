🏢 API de Reserva de Salas - Nível 1

Esta API foi desenvolvida para gerir o agendamento de salas de reunião, garantindo que não existam conflitos de horários e permitindo um fluxo organizado de reservas. O projeto foca em boas práticas de desenvolvimento, persistência de dados e containerização.

🚀 Funcionalidades Atuais

Gestão de Usuários: CRUD completo para registo de colaboradores.

Gestão de Salas: Registo de salas com capacidade e estado (ATIVA/INATIVA).

Sistema de Reservas:

Validação de conflitos (impede sobreposição de horários na mesma sala).

Suporte a intervalos semiabertos (uma reserva pode começar exatamente quando outra termina).

Fluxo de cancelamento de reservas.

Validações de Domínio: Erros amigáveis para datas inválidas ou reservas em salas inativas.

🛠️ Tecnologias Utilizadas

Java 17 & Spring Boot 3

Spring Data JPA: Abstração de persistência.

PostgreSQL: Base de dados relacional.

Flyway: Gestão de migrações e evolução do esquema da base de dados.

Docker & Docker Compose: Orquestração de containers.

Maven: Gestão de dependências e build.

JUnit 5 & Mockito: Testes de unidade e validação de regras de negócio.

📦 Como Executar o Projeto

Pré-requisitos

Docker e Docker Compose instalados.

Passo a Passo

Clone o repositório para a sua máquina.

No terminal, na raiz do projeto, execute:

docker-compose up --build -d


A aplicação estará disponível em http://localhost:8080/api/v1.

O PostgreSQL estará acessível na porta 5440.

📡 Endpoints Principais

Recurso

Método

Endpoint

Descrição

Usuários

POST

/usuarios

Cria um novo usuário

Usuários

GET

/usuarios

Lista todos os usuários

Salas

POST

/salas

Regista uma nova sala

Salas

GET

/salas

Lista todas as salas

Reservas

POST

/reservas

Cria um agendamento (valida conflitos)

Reservas

GET

/reservas

Lista todas as reservas

Reservas

PATCH

/reservas/{id}/cancelar

Cancela uma reserva ativa

🧪 Testes de Unidade

Para validar as regras de negócio e a lógica de conflitos de horário, execute:

./mvnw test


📂 Estrutura de Pastas

src/main/java: Lógica da aplicação (Controllers, Services, Repositories).

src/main/resources/db/migration: Scripts SQL de migração da base de dados.

Dockerfile: Instruções para criação da imagem da aplicação.

docker-compose.yml: Definição dos serviços (API e Base de Dados).

Desenvolvido como parte do desafio de Prática Profissional - Nível 1.