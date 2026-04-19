# ==========================================
# ESTÁGIO 1: BUILD (Compilação)
# ==========================================
# Usamos a imagem oficial do Maven com Java 17
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

# Criamos uma pasta de trabalho dentro do container
WORKDIR /app

# Copiamos o pom.xml e baixamos as dependências primeiro (Cache de camadas do Docker)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiamos o código fonte
COPY src ./src

# Compilamos a aplicação pulando os testes (os testes já rodam no GitHub Actions)
RUN mvn clean package -DskipTests

# ==========================================
# ESTÁGIO 2: EXECUÇÃO (Produção)
# ==========================================
# Usamos um Linux minúsculo (Alpine) apenas com o JRE (Java Runtime Environment)
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Garantimos que a aplicação use UTF-8 para não ter problemas com acentuação
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8

# Copiamos apenas o arquivo .jar gerado no Estágio 1.
# Isso reduz o tamanho da imagem de ~800MB para ~200MB!
COPY --from=build /app/target/*.jar app.jar

# Comando que o Docker executará ao ligar o container
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]