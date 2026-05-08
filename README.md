🏢 RoomRes - Sistema Avançado de Reserva de Salas (Cloud-Native)

O RoomRes evoluiu de uma API monolítica simples para uma Arquitetura de Microsserviços Cloud-Native altamente escalável, resiliente e distribuída. Este projeto tem como objetivo gerenciar reservas de salas corporativas, garantindo integridade de dados, comunicação assíncrona, notificações em tempo real e observabilidade.

🚀 Principais Funcionalidades e Destaques Arquiteturais

Autenticação Híbrida e Segura: Login via OAuth2 (GitHub) e autenticação baseada em JWT.

Comunicação Assíncrona e Mensageria:

RabbitMQ: Roteamento de e-mails transacionais e eventos de notificação em tempo real.

Apache Kafka: Registro imutável de trilhas de auditoria (Audit Log) processando alto volume de eventos.

Notificações em Tempo Real (SSE): Arquitetura Server-Sent Events com "Modo Guerrilha" (reconhecimento automático de proxies e limites dinâmicos para furar bloqueios de buffer do Cloudflare/Nginx).

Caching Distribuído: Utilização de Redis no room-service para respostas ultrarrápidas do catálogo de salas.

Paridade de Ambientes: Perfis dinâmicos do Spring (dev e prod) que reagem automaticamente se a aplicação está rodando localmente (Docker Compose) ou na nuvem (Kubernetes).

CI/CD Total: Pipelines no GitHub Actions realizando builds, testes com Testcontainers e push automatizado de imagens para o Docker Hub.

🧩 Arquitetura de Microsserviços

O ecossistema é composto por 8 serviços independentes operando por trás de um API Gateway:

gateway-service (Porta 8080): Ponto de entrada único usando Spring Cloud Gateway. Faz o roteamento e validação inicial.

discovery-service (Porta 8761): Netflix Eureka Server. Serviço de registro e descoberta dinâmica de instâncias.

user-service: Gestão de usuários, perfis, integração OAuth2 e emissão de JWT.

room-service: Catálogo completo de salas, capacidades e status, com caching em Redis.

booking-service: Core business. Impede sobreposição de horários e gerencia o ciclo de vida da reserva. Publica eventos para RabbitMQ e Kafka.

notification-service: Consumidor assíncrono. Dispara e-mails de confirmação/cancelamento e empurra eventos SSE diretamente para o Front-end.

audit-service: Serviço de Compliance. Ouve os tópicos do Kafka e registra todas as ações críticas em um banco de dados isolado.

suggestion-service: Algoritmo dedicado a recomendar salas alternativas quando há conflitos de horário.

🛠️ Stack Tecnológica

Backend & Core:

Java 17 | Spring Boot 3

Spring Cloud (Gateway, Netflix Eureka, OpenFeign)

Spring Security | OAuth2 | JWT

Bancos de Dados & Caching:

PostgreSQL (4 instâncias isoladas: User, Room, Booking, Audit)

Redis (Caching de Salas)

Flyway (Database Migrations)

Mensageria & Streaming:

RabbitMQ

Apache Kafka & Zookeeper

DevOps & Infraestrutura:

Docker & Docker Compose

Kubernetes (Minikube)

GitHub Actions (CI/CD)

Cloudflare Tunnels (Acesso Externo Seguro)

Testcontainers (Testes de Integração Descartáveis)

Observabilidade (Fase 3):

Zipkin (Distributed Tracing)

Prometheus (Metrics)

Grafana (Dashboards)

📦 Como Executar o Projeto (Modo Produção)

A infraestrutura foi desenhada para subir com um único comando usando as imagens compiladas pela pipeline de CI/CD, não havendo necessidade de compilar o código fonte localmente.

Pré-requisitos

Docker e Docker Compose instalados.

Conta no GitHub (para autenticação OAuth2).

1. Configuração do Ambiente

Crie um arquivo .env na raiz do projeto com as seguintes variáveis:

GITHUB_CLIENT_ID=sua_chave_aqui
GITHUB_CLIENT_SECRET=seu_secret_aqui

GITHUB_CLIENT_ID_DEV=sua_chave_local_aqui
GITHUB_CLIENT_SECRET_DEV=seu_secret_local_aqui

MAIL_USERNAME=seu_email@gmail.com
MAIL_PASSWORD=sua_senha_de_aplicativo


2. Subindo a Arquitetura Completa

No terminal, execute:

docker-compose up -d


O Docker irá baixar o ecossistema completo (PostgreSQL, Kafka, RabbitMQ, Redis, Microsserviços e o Front-end em Angular) diretamente do Docker Hub.

A Interface da Aplicação estará disponível em: http://localhost:4200

O Eureka Server (Dashboard) estará em: http://localhost:8761

O RabbitMQ Management estará em: http://localhost:15672



Desenvolvido com foco em escalabilidade, resiliência e boas práticas de engenharia de software.