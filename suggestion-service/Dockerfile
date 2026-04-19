# ESTÁGIO 1: BUILD
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# ESTÁGIO 2: EXECUÇÃO
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
ENV LANG=C.UTF-8

# O Quarkus gera vários diretórios pequenos em vez de um "Fat Jar" gigante.
# Precisamos copiar a estrutura correta (Fast-Jar)
COPY --from=build /app/target/quarkus-app/lib/ /app/lib/
COPY --from=build /app/target/quarkus-app/*.jar /app/
COPY --from=build /app/target/quarkus-app/app/ /app/app/
COPY --from=build /app/target/quarkus-app/quarkus/ /app/quarkus/

# Executa o Quarkus
ENTRYPOINT ["java", "-jar", "/app/quarkus-run.jar"]