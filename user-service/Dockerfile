# ==========================================
# ESTÁGIO 1: BUILD (Compilação)
# ==========================================
# Usamos a imagem oficial do Maven com Java 17
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

# Criamos uma pasta de trabalho dentro do container
WORKDIR /app

# Copiamos o pom.xml e o código fonte juntos
COPY pom.xml .
COPY src ./src

# Compilamos a aplicação pulando os testes
# O Maven fará o download apenas das dependências reais necessárias (ignorando plugins inúteis de documentação)
RUN mvn clean package -DskipTests

# ==========================================
# ESTÁGIO 2: EXECUÇÃO (Produção)
# ==========================================
# Usamos um Linux minúsculo (Alpine) apenas com o JRE
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Garantimos que a aplicação use UTF-8
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8

# Copiamos apenas o arquivo .jar gerado no Estágio 1
COPY --from=build /app/target/*.jar app.jar

# Comando que o Docker executará ao ligar o container
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]