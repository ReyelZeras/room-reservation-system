# --- Estágio 1: Build da aplicação ---
FROM maven:3.8.5-eclipse-temurin-17 AS build
WORKDIR /app

# Força o encoding do ambiente para UTF-8 para o compilador e logs
ENV MAVEN_OPTS="-Dfile.encoding=UTF-8"

# Copia apenas o pom.xml primeiro para aproveitar o cache das dependências
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o código fonte
COPY src ./src

# Executamos o build.
# A estratégia aqui é desativar a filtragem de recursos.
# Isso impede que o Maven tente ler e processar o conteúdo dos arquivos .properties
# durante a cópia, evitando o erro de encoding (MalformedInputException).
RUN mvn clean package -DskipTests \
    -Dproject.build.sourceEncoding=UTF-8 \
    -Dproject.reporting.outputEncoding=UTF-8 \
    -Dmaven.resources.filtering=false

# --- Estágio 2: Execução ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Garante que o container de execução use UTF-8 para logs e saída de dados
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Define o encoding na inicialização da JVM
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-jar", "app.jar"]